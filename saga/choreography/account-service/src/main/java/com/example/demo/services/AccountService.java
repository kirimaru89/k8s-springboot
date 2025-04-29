package com.example.demo.services;

import com.example.demo.adapter.kafka.producer.KafkaProducer;
import com.example.demo.dto.message.fraud.CheckFraudTransactionMessageDto;
import com.example.demo.dto.message.fraud.FraudTransactionDetectedMessageDto;
import com.example.demo.dto.message.fraud.FraudTransactionVerifiedMessageDto;
import com.example.demo.dto.message.TransactionInitiatedMessageDto;
import com.example.demo.dto.message.transaction.UpdateTransactionStatusDto;
import com.example.demo.dto.request.account.CreateAccountRequestDto;
import com.example.demo.dto.response.ApiResponseDto;
import com.example.demo.dto.response.account.CreateAccountResponseDto;
import com.example.demo.dto.response.account.GetAccountResponseDto;
import com.example.demo.dto.response.account.ListAccountsResponseDto;
import com.example.demo.enums.AccountReservationStatus;
import com.example.demo.enums.ResponseCode;
import com.example.demo.models.Account;
import com.example.demo.models.AccountReservation;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.AccountReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.example.demo.mapper.AccountMapper.*;

@Slf4j
@Service
public class AccountService {

    @Value("${spring.kafka.producer.topic.account.balance.completed}")
    private String accountBalanceCompletedTopic ;
    @Value("${spring.kafka.producer.topic.account.balance.rollback}")
    private String accountBalanceRolledbackTopic ;
    @Value("${spring.kafka.producer.topic.account.balance.reserved}")
    private String accountBalanceReservedTopic ;

    private final AccountRepository accountRepository;
    private final AccountReservationRepository accountReservationRepository;
    private final TransactionTemplate txTemplate;
    private final KafkaProducer kafkaProducer;

    public AccountService(
            AccountRepository accountRepository,
            AccountReservationRepository accountReservationRepository,
            PlatformTransactionManager transactionManager,
            KafkaProducer kafkaProducer) {
        this.accountRepository = accountRepository;
        this.accountReservationRepository = accountReservationRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.kafkaProducer = kafkaProducer;
    }

    // restful api
    public CreateAccountResponseDto createAccount(CreateAccountRequestDto request) {
        Double initialBalance = 0.0;
        
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setBalance(initialBalance);
        accountRepository.save(account);

        return toCreateAccountResponse(account);
    }

    public GetAccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }

        return toAccountResponse(account);
    }
   
    public ListAccountsResponseDto getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        if (accounts == null || accounts.isEmpty()) {
            throw new EntityNotFoundException("Accounts with user_id " + userId + " not found");
        }

        return toListAccountResponse(accounts);
    }

    // saga
    public void completeTransaction(FraudTransactionVerifiedMessageDto transactionMessage) {
        AccountReservation accountReservation = accountReservationRepository.findByTransactionId(transactionMessage.getTransactionId()).orElse(null);
        if (accountReservation == null) {
            log.error("Account reservation with id {} not found. Transaction aborted.", transactionMessage.getTransactionId());
            return;
        }

        switch (accountReservation.getType()) {
            case DEPOSIT -> completeDeposit(accountReservation);
            case WITHDRAW -> completeWithdraw(accountReservation);
            case TRANSFER -> completeTransfer(accountReservation);
        }
    }

    private void completeDeposit(AccountReservation accountReservation) {
        txTemplate.executeWithoutResult(status -> {
            Account account = accountRepository.findById(accountReservation.getAccountId()).orElse(null);
            if (account == null) {
                log.error("Account with id {} not found. Transaction aborted.", accountReservation.getAccountId());
                return;
            }

            account.setBalance(account.getBalance() + accountReservation.getAmount());
            accountRepository.save(account);

            accountReservation.setStatus(AccountReservationStatus.SUCCESS);
            accountReservationRepository.save(accountReservation);

            sendAccountBalanceCompletedMessage(accountReservation.getTransactionId());
        });
    }

    private void completeWithdraw(AccountReservation accountReservation) {
        txTemplate.executeWithoutResult(status -> {
            accountReservation.setStatus(AccountReservationStatus.SUCCESS);
            accountReservationRepository.save(accountReservation);

            sendAccountBalanceCompletedMessage(accountReservation.getTransactionId());
        });
    }

    private void completeTransfer(AccountReservation accountReservation) {
        completeWithdraw(accountReservation);
    }

    public void rollbackTransaction(FraudTransactionDetectedMessageDto transactionMessage) {
        AccountReservation accountReservation = accountReservationRepository.findByTransactionId(transactionMessage.getTransactionId()).orElse(null);
        if (accountReservation == null) {
            log.error("Account reservation with id {} not found. Transaction aborted.", transactionMessage.getTransactionId());
            return;
        }

        switch (accountReservation.getType()) {
            case DEPOSIT -> rollbackDeposit(accountReservation);
            case WITHDRAW -> rollbackWithdraw(accountReservation);
            case TRANSFER -> rollbackTransfer(accountReservation);
        }
    }

    private void rollbackDeposit(AccountReservation accountReservation) {
        txTemplate.executeWithoutResult(status -> {
            accountReservation.setStatus(AccountReservationStatus.FAILURE);
            accountReservationRepository.save(accountReservation);

            sendAccountBalanceRollbackMessage(accountReservation.getTransactionId());
        });
    }

    private void rollbackWithdraw(AccountReservation accountReservation) {
        txTemplate.executeWithoutResult(status -> {
            Account account = accountRepository.findById(accountReservation.getAccountId()).orElse(null);
            if (account == null) {
                log.error("Account with id {} not found. Transaction aborted.", accountReservation.getAccountId());
                return;
            }

            account.setBalance(account.getBalance() + accountReservation.getAmount());
            accountRepository.save(account);

            accountReservation.setStatus(AccountReservationStatus.FAILURE);
            accountReservationRepository.save(accountReservation);

            sendAccountBalanceRollbackMessage(accountReservation.getTransactionId());
        });
    }

    private void rollbackTransfer(AccountReservation accountReservation) {
        rollbackWithdraw(accountReservation);
    }
   
    public void transaction(TransactionInitiatedMessageDto transactionMessage) {
        switch (transactionMessage.getType()) {
            case DEPOSIT -> deposit(transactionMessage);
            case WITHDRAW -> withdraw(transactionMessage);
            case TRANSFER -> transfer(transactionMessage);
        }
    }

    private void deposit(TransactionInitiatedMessageDto transactionMessage) {
         txTemplate.executeWithoutResult(status -> {
            Account account = accountRepository.findById(transactionMessage.getAccountId()).orElse(null);
            if (account == null) {
                sendAccountBalanceRollbackMessage(transactionMessage.getTransactionId());
                return;
            }

            accountReservationRepository.save(toAccountReservation(transactionMessage));

            sendAccountBalanceReservedMessage(transactionMessage);
        });
    }

    private void withdraw(TransactionInitiatedMessageDto transactionMessage) {
        txTemplate.executeWithoutResult(status -> {
            Account account = accountRepository.findById(transactionMessage.getAccountId()).orElse(null);
            if (account == null) {
                sendAccountBalanceRollbackMessage(transactionMessage.getTransactionId());
                return;
            }

            if (account.getBalance() < transactionMessage.getAmount()) {
                sendAccountBalanceRollbackMessage(transactionMessage.getTransactionId());
                return;
            }


            account.setBalance(account.getBalance() - transactionMessage.getAmount());
            accountRepository.save(account);

            accountReservationRepository.save(toAccountReservation(transactionMessage));

            sendAccountBalanceReservedMessage(transactionMessage);
        });
    }

    private void transfer(TransactionInitiatedMessageDto transactionMessage) {
        withdraw(transactionMessage);
    }

    private void sendAccountBalanceReservedMessage(TransactionInitiatedMessageDto transactionMessage) {
        String messageKey = transactionMessage.getTransactionId().toString();
        CheckFraudTransactionMessageDto checkFraudTransactionMessageDto = new CheckFraudTransactionMessageDto();
        checkFraudTransactionMessageDto.setTransactionId(transactionMessage.getTransactionId());
        checkFraudTransactionMessageDto.setAccountId(transactionMessage.getAccountId());
        checkFraudTransactionMessageDto.setAmount(transactionMessage.getAmount());
        checkFraudTransactionMessageDto.setType(transactionMessage.getType());

        String messageValue = toMessage(checkFraudTransactionMessageDto);
        kafkaProducer.sendMessage(accountBalanceReservedTopic, messageKey, messageValue);
    }

    private void sendAccountBalanceCompletedMessage(Long transactionId) {
        String messageKey = transactionId.toString();
        UpdateTransactionStatusDto updateTransactionStatusDto = new UpdateTransactionStatusDto ();
        updateTransactionStatusDto.setTransactionId(transactionId);

        String messageValue = toMessage(updateTransactionStatusDto);
        kafkaProducer.sendMessage(accountBalanceCompletedTopic, messageKey, messageValue);
    }

    private void sendAccountBalanceRollbackMessage(Long transactionId) {
        String messageKey = transactionId.toString();
        UpdateTransactionStatusDto updateTransactionStatusDto = new UpdateTransactionStatusDto ();
        updateTransactionStatusDto.setTransactionId(transactionId);

        String messageValue = toMessage(updateTransactionStatusDto);
        kafkaProducer.sendMessage(accountBalanceRolledbackTopic, messageKey, messageValue);
    }
} 