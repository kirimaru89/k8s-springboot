package com.example.demo.mapper;

import com.example.demo.dto.message.TransactionInitiatedMessageDto;
import com.example.demo.dto.response.account.ListTransactionsResponseDto;
import com.example.demo.dto.response.account.TransactionResponseDto;
import com.example.demo.models.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TransactionMapper {
    // message
    public static String toMessage(Transaction transaction) {
        try {
            TransactionInitiatedMessageDto transactionMessage = TransactionInitiatedMessageDto.builder()
                    .transactionId(transaction.getId())
                    .accountId(transaction.getAccountId())
                    .amount(transaction.getAmount())
                    .type(transaction.getType())
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(transactionMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to " + e.getMessage(), e);
        }
    }

    public static <T> T parseMessage(String jsonStrMessage, TypeReference<T> typeReference) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonStrMessage, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message", e);
        }
    }

    // response
    public static TransactionResponseDto toTransactionResponse(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .build();
    }

    public static ListTransactionsResponseDto toListTransactionResponse(List<Transaction> transactions) {
        return ListTransactionsResponseDto.builder()
                .transactions(transactions.stream().map(TransactionMapper::toTransactionResponse).toList())
                .build();
    }

}
