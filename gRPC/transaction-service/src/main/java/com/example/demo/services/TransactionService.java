package com.example.demo.services;

import com.example.demo.adapter.grpc.client.AccountGrpcClient;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import digitaldocuments.library.v1.AccountMessages;
import digitaldocuments.library.v1.TransactionMessages;
import org.springframework.stereotype.Service;

import static com.example.demo.mapper.TransactionMapper.*;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountGrpcClient accountGrpcClient;

    public TransactionService(TransactionRepository transactionRepository, AccountGrpcClient accountGrpcClient) {
        this.transactionRepository = transactionRepository;
        this.accountGrpcClient = accountGrpcClient;
    }

    public TransactionMessages.Transaction createTransaction(TransactionMessages.CreateTransactionRequest request) {
        // create transaction
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setType(toDomainType(request.getType()));
        transaction.setStatus(TransactionStatus.INITIATED);
        transactionRepository.save(transaction);

        // update balance
        AccountMessages.UpdateAccountBalanceRequest updateAccountBalanceRequest = AccountMessages.UpdateAccountBalanceRequest.newBuilder()
                .setTransactionId(transaction.getId())
                .setAccountId(request.getAccountId())
                .setAmount(request.getAmount())
                .setType(request.getType())
                .build();
        try {
            AccountMessages.Account account = accountGrpcClient.updateAccountBalance(updateAccountBalanceRequest);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

} 