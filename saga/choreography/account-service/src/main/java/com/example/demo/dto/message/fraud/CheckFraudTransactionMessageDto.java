package com.example.demo.dto.message.fraud;

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
public class CheckFraudTransactionMessageDto {
    Long transactionId;
    Long accountId;
    Double amount;
    TransactionType type;
}
