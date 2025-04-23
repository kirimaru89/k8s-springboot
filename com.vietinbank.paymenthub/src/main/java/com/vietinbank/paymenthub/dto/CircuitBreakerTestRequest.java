package com.vietinbank.paymenthub.dto;

public class CircuitBreakerTestRequest {
    private String bankName;  // "BANK_A" hoặc "BANK_B"
    private String operation; // Loại operation muốn test

    // Getters and Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
} 