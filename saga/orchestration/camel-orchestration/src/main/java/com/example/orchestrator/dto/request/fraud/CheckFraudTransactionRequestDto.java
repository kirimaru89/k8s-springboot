package com.example.orchestrator.dto.request.fraud;

import com.example.orchestrator.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckFraudTransactionRequestDto {
    Long transactionId;
    Long accountId;
    Double amount;
    TransactionType type;
}
