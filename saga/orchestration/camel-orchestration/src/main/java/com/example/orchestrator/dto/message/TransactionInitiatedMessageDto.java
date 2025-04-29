package com.example.orchestrator.dto.message;

import com.example.orchestrator.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInitiatedMessageDto {
    private Long transactionId;
    private Long accountId;
    private Double amount;
    private TransactionType type;
}
