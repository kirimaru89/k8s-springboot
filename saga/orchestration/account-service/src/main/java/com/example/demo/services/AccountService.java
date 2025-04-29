package com.example.demo.services;

import com.example.demo.dto.message.FraudTransactionDetectedMessageDto;
import com.example.demo.dto.message.FraudTransactionVerifiedMessageDto;
import com.example.demo.dto.message.TransactionInitiatedMessageDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.example.demo.mapper.AccountMapper.*;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountReservationRepository accountReservationRepository;
    private final TransactionTemplate txTemplate;

    public AccountService(
            AccountRepository accountRepository,
            AccountReservationRepository accountReservationRepository,
            PlatformTransactionManager transactionManager) {
        this.accountRepository = accountRepository;
        this.accountReservationRepository = accountReservationRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
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
        });
    }

    private void completeWithdraw(AccountReservation accountReservation) {
        txTemplate.executeWithoutResult(status -> {
            accountReservation.setStatus(AccountReservationStatus.SUCCESS);
            accountReservationRepository.save(accountReservation);
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
        });
    }

    private void rollbackTransfer(AccountReservation accountReservation) {
        rollbackWithdraw(accountReservation);
    }
   
    public ResponseEntity<ApiResponseDto<Void>> transaction(TransactionInitiatedMessageDto transactionMessage) {
        switch (transactionMessage.getType()) {
            case DEPOSIT -> {
                return deposit(transactionMessage);
            }
            case WITHDRAW -> {
                return withdraw(transactionMessage);
            }
            case TRANSFER -> {
                return transfer(transactionMessage);
            }
            default -> {
                return ApiResponseDto.error(ResponseCode.BAD_REQUEST, "Invalid transaction type");
            }
        }
    }

    private ResponseEntity<ApiResponseDto<Void>> deposit(TransactionInitiatedMessageDto transactionMessage) {
        return txTemplate.execute(status -> {
            Account account = accountRepository.findById(transactionMessage.getAccountId()).orElse(null);
            if (account == null) {
                return ApiResponseDto.error(ResponseCode.BAD_REQUEST,"Account not found. Transaction aborted.");
            }

            accountReservationRepository.save(toAccountReservation(transactionMessage));

            return ApiResponseDto.success(null);
        });
    }

    private ResponseEntity<ApiResponseDto<Void>> withdraw(TransactionInitiatedMessageDto transactionMessage) {
        return txTemplate.execute(status -> {
            Account account = accountRepository.findById(transactionMessage.getAccountId()).orElse(null);
            if (account == null) {
                return ApiResponseDto.error(ResponseCode.BAD_REQUEST, "Account not found. Transaction aborted.");
            }

            if (account.getBalance() < transactionMessage.getAmount()) {
                return ApiResponseDto.error( ResponseCode.CONFLICT,"Insufficient funds. Transaction aborted.");
            }


            account.setBalance(account.getBalance() - transactionMessage.getAmount());
            accountRepository.save(account);

            accountReservationRepository.save(toAccountReservation(transactionMessage));
            return ApiResponseDto.success(null);
        });
    }

    private ResponseEntity<ApiResponseDto<Void>> transfer(TransactionInitiatedMessageDto transactionMessage) {
        return withdraw(transactionMessage);
    }


} 