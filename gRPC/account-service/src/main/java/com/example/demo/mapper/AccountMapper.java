package com.example.demo.mapper;

import com.example.demo.enums.AccountReservationStatus;
import com.example.demo.enums.TransactionType;
import com.example.demo.models.Account;
import com.example.demo.models.AccountReservation;
import digitaldocuments.library.v1.AccountMessages;
import digitaldocuments.library.v1.TransactionEnums;

import java.util.List;
import java.util.stream.Collectors;

public class AccountMapper {
    //
    public static AccountReservation toAccountReservation(AccountMessages.UpdateAccountBalanceRequest transactionMessage) {
        AccountReservation accountReservation = new AccountReservation();
        accountReservation.setTransactionId(transactionMessage.getTransactionId());
        accountReservation.setAccountId(transactionMessage.getAccountId());
        accountReservation.setAmount(transactionMessage.getAmount());
        accountReservation.setType(toDomainType(transactionMessage.getType()));
        accountReservation.setStatus(AccountReservationStatus.RESERVED);
        return accountReservation;
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

    // response
    public static AccountMessages.Account toAccountResponse(Account account) {
        return AccountMessages.Account.newBuilder()
                .setId(account.getId())
                .setUserId(account.getUserId())
                .setBalance(account.getBalance())
                .build();
    }

    public static AccountMessages.ListAccountsResponse toListAccountResponse(List<Account> accounts) {//
        return AccountMessages.ListAccountsResponse.newBuilder()
                .addAllAccounts(accounts
                        .stream()
                        .map(AccountMapper::toAccountResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public static AccountMessages.Account toCreateAccountResponse(Account account) {
        return AccountMessages.Account.newBuilder()
                .setId(account.getId())
                .setUserId(account.getUserId())
                .setBalance(account.getBalance())
                .build();
    }

}
