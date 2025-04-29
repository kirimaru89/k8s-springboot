package com.example.demo.services;

import com.example.demo.adapter.grpc.client.FraudDetectionGrpcClient;
import com.example.demo.models.Account;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.AccountReservationRepository;
import digitaldocuments.library.v1.AccountMessages;
import digitaldocuments.library.v1.FraudDetectionMessages;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.example.demo.mapper.AccountMapper.*;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionTemplate txTemplate;
    private final FraudDetectionGrpcClient fraudDetectionGrpcClient;

    public AccountService(
            AccountRepository accountRepository,
            AccountReservationRepository accountReservationRepository,
            PlatformTransactionManager transactionManager,
            FraudDetectionGrpcClient fraudDetectionGrpcClient
            ) {
        this.accountRepository = accountRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.fraudDetectionGrpcClient = fraudDetectionGrpcClient;
    }

    // gRPC
    @Transactional
    public AccountMessages.Account createAccount(AccountMessages.CreateAccountRequest request) {
        Double initialBalance = 0.0;
        
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setBalance(initialBalance);
        accountRepository.save(account);

        return toCreateAccountResponse(account);
    }

    public AccountMessages.Account getAccountById(AccountMessages.GetAccountRequest request) {
        Account account = accountRepository.findById(request.getId()).orElse(null);
        if (account == null) {
            throw new EntityNotFoundException("[AccountService] Account with id " + request.getId() + " not found");
        }

        return toAccountResponse(account);
    }

    public AccountMessages.ListAccountsResponse getAccountsByUserId(AccountMessages.ListAccountsRequest request) {
        List<Account> accounts = accountRepository.findByUserId(request.getUserId());

        if (accounts == null || accounts.isEmpty()) {
            throw new EntityNotFoundException("[AccountService] Accounts with user_id " + request.getUserId() + " not found");
        }

        return toListAccountResponse(accounts);
    }


    public AccountMessages.Account updateAccountBalance(AccountMessages.UpdateAccountBalanceRequest updateAccountBalanceRequest) {
        FraudDetectionMessages.CreateFraudDetectionRequest request = FraudDetectionMessages.CreateFraudDetectionRequest.newBuilder()
                .setTransactionId(updateAccountBalanceRequest.getTransactionId())
                .setAccountId(updateAccountBalanceRequest.getAccountId())
                .setAmount(updateAccountBalanceRequest.getAmount())
                .setType(updateAccountBalanceRequest.getType())
                .build();
        FraudDetectionMessages.FraudDetection fraudDetection = fraudDetectionGrpcClient.createFraudDetection(request);

        if (fraudDetection.getIsFraudulent()) {
            throw new IllegalArgumentException("[FraudDetectionService] " + fraudDetection.getReason());
        }

        switch (updateAccountBalanceRequest.getType()) {
            case DEPOSIT -> {
                return deposit(updateAccountBalanceRequest);
            }
            case WITHDRAW -> {
                return withdraw(updateAccountBalanceRequest);
            }
            case TRANSFER -> {
                return transfer(updateAccountBalanceRequest);
            }
            default -> {
                throw new IllegalArgumentException("[AccountService] Transaction type cannot be UNKNOWN");
            }
        }
    }

    private AccountMessages.Account deposit(AccountMessages.UpdateAccountBalanceRequest updateAccountBalanceRequest) {
        return txTemplate.execute(status -> {
            Account account = accountRepository.findById(updateAccountBalanceRequest.getAccountId()).orElse(null);
            if (account == null) {
                throw new EntityNotFoundException("[AccountService] Account with id " + updateAccountBalanceRequest.getAccountId() + " not found");
            }

            account.setBalance(account.getBalance() + updateAccountBalanceRequest.getAmount());
            accountRepository.save(account);

            return toAccountResponse(account);
        });
    }

    private AccountMessages.Account withdraw(AccountMessages.UpdateAccountBalanceRequest updateAccountBalanceRequest) {
        return txTemplate.execute(status -> {
            Account account = accountRepository.findById(updateAccountBalanceRequest.getAccountId()).orElse(null);
            if (account == null) {
                throw new EntityNotFoundException("[AccountService] Account with id " + updateAccountBalanceRequest.getAccountId() + " not found");
            }

            if (account.getBalance() < updateAccountBalanceRequest.getAmount()) {
                throw new IllegalArgumentException("[AccountService] Insufficient funds. Transaction aborted.");
            }


            account.setBalance(account.getBalance() - updateAccountBalanceRequest.getAmount());
            accountRepository.save(account);

            return toAccountResponse(account);
        });
    }

    private AccountMessages.Account transfer(AccountMessages.UpdateAccountBalanceRequest transactionMessage) {
        return withdraw(transactionMessage);
    }
} 