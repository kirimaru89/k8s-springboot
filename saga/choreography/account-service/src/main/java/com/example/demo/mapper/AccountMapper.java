package com.example.demo.mapper;

import com.example.demo.dto.message.TransactionInitiatedMessageDto;
import com.example.demo.dto.response.account.CreateAccountResponseDto;
import com.example.demo.dto.response.account.GetAccountResponseDto;
import com.example.demo.dto.response.account.ListAccountsResponseDto;
import com.example.demo.enums.AccountReservationStatus;
import com.example.demo.models.Account;
import com.example.demo.models.AccountReservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

public class AccountMapper {
    //
    public static AccountReservation toAccountReservation(TransactionInitiatedMessageDto transactionMessage) {
        AccountReservation accountReservation = new AccountReservation();
        accountReservation.setTransactionId(transactionMessage.getTransactionId());
        accountReservation.setAccountId(transactionMessage.getAccountId());
        accountReservation.setAmount(transactionMessage.getAmount());
        accountReservation.setType(transactionMessage.getType());
        accountReservation.setStatus(AccountReservationStatus.RESERVED);
        return accountReservation;
    }

    // response
    public static GetAccountResponseDto toAccountResponse(Account account) {
        return GetAccountResponseDto.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .build();
    }

    public static ListAccountsResponseDto toListAccountResponse(List<Account> accounts) {//
        return ListAccountsResponseDto.builder()
                .accounts(accounts.stream().map(AccountMapper::toAccountResponse).toList())
                .build();
    }

    public static CreateAccountResponseDto toCreateAccountResponse(Account account) {
        return CreateAccountResponseDto.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .build();
    }

    // message
    public static <T> String toMessage(T t) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize account response", e);
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

}
