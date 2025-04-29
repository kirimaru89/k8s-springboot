package com.example.demo.mapper;

import com.example.demo.enums.TransactionType;
import com.example.demo.models.Transaction;
import digitaldocuments.library.v1.TransactionEnums;
import digitaldocuments.library.v1.TransactionMessages;

public class TransactionMapper {

    // response
    public static TransactionMessages.Transaction toTransactionResponse(Transaction transaction) {
        return TransactionMessages.Transaction.newBuilder()
                .setId(transaction.getId())
                .setAccountId(transaction.getAccountId())
                .setAmount(transaction.getAmount())
                .setType(toProtoType(transaction.getType()))
                .build();
    }

    public static TransactionType toDomainType(TransactionEnums.TransactionType protoType) {
        if (protoType == TransactionEnums.TransactionType.UNKNOWN) {
            throw new IllegalArgumentException("Transaction type cannot be UNKNOWN");
        }
        return TransactionType.valueOf(protoType.name());
    }

    public static TransactionEnums.TransactionType toProtoType(TransactionType domainType) {
        return TransactionEnums.TransactionType.valueOf(domainType.name());
    }
}
