package com.example.step2service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String transactionId;
    private String payload;
    private String status;
    private String step;
    private String errorMessage;
} 