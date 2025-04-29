package com.example.demo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudTransactionDetectedMessageDto {
    private Long transactionId;
    private String reason;
}
