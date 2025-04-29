package com.example.demo.mapper;

import com.example.demo.dto.response.account.ListTransactionsResponseDto;
import com.example.demo.dto.response.account.TransactionResponseDto;
import com.example.demo.models.Transaction;

import java.util.List;

public class TransactionMapper {
    // response
    public static TransactionResponseDto toTransactionResponse(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .build();
    }

    public static ListTransactionsResponseDto toListTransactionResponse(List<Transaction> transactions) {
        return ListTransactionsResponseDto.builder()
                .transactions(transactions.stream().map(TransactionMapper::toTransactionResponse).toList())
                .build();
    }

}
