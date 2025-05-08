package com.example.step1service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String transactionId;
    private String payload; // The string payload as per requirement
    private String status; // e.g., "SUCCESS", "FAILURE"
    private String step; // e.g., "STEP1", "STEP2"
    private String errorMessage; // Optional error message on failure
} 