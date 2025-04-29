package com.example.demo.dto.request.fraud;

import com.example.demo.enums.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckFraudTransactionRequestDto {
    Long transactionId;
    Long accountId;
    Double amount;
    TransactionType type;
}
