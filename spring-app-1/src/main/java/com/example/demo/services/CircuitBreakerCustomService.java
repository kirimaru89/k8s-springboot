package com.example.demo.services;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dto.CircuitBreakerTestRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import com.example.demo.dto.ApiResponse;
import com.example.demo.enums.ResponseCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.concurrent.TimeoutException;

@Service
public class CircuitBreakerCustomService {
    private static final String BACKEND = "example";
    private static final String BANK_A_BACKEND = "bank-a";
    private static final String BANK_B_BACKEND = "bank-b";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;

    @Autowired
    public CircuitBreakerCustomService(CircuitBreakerRegistry circuitBreakerRegistry, 
                                     MeterRegistry meterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.meterRegistry = meterRegistry;
    }

    public ResponseEntity<ApiResponse<String>> testWithCustomConfig(CircuitBreakerTestRequest request) {
        try {
            String bankName = request.getBankName().toUpperCase();
            CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(bankName);
            return handleBankOperation(circuitBreaker, bankName, request.getOperation());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCode.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<String>> handleBankOperation(CircuitBreaker circuitBreaker, String bankName, String operation) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                // Giả lập xử lý nghiệp vụ
                if ("fail".equalsIgnoreCase(operation)) {
                    recordMetrics(bankName, operation, false);
                    return ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, 
                        String.format("Simulated failure for bank: %s", bankName));
                }
                if ("timeout".equalsIgnoreCase(operation)) {
                    try {
                        // Bank A xử lý chậm hơn Bank B
                        long sleepTime = "BANK_A".equals(bankName) ? 2000 : 1000;
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        recordMetrics(bankName, operation, false);
                        return ApiResponse.error(ResponseCode.TIMEOUT_ERROR, 
                            String.format("Operation timeout for bank: %s", bankName));
                    }
                }

                // Xử lý tỷ lệ lỗi khác nhau cho từng bank
                double failureRate = "BANK_A".equals(bankName) ? 0.4 : 0.2; // Bank A có tỷ lệ lỗi cao hơn
                if (Math.random() < failureRate) {
                    recordMetrics(bankName, operation, false);
                    return ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR,
                        String.format("Random failure for bank: %s", bankName));
                }

                recordMetrics(bankName, operation, true);
                return ApiResponse.success("Operation successful for bank: " + bankName);
            });
        } catch (Exception e) {
            return handleException(e, bankName);
        }
    }

    private void recordMetrics(String bankName, String operation, boolean isSuccess) {
        meterRegistry.counter("circuit_breaker_requests_total",
            "bank", bankName,
            "operation", operation,
            "status", isSuccess ? "success" : "failure"
        ).increment();
    }

    

    private ResponseEntity<ApiResponse<String>> handleException(Exception e, String bankName) {
        Span span = Span.current();
        span.recordException(e);
        span.setStatus(StatusCode.ERROR, "There is an error");

        if (e instanceof CallNotPermittedException) {
            return ApiResponse.error(ResponseCode.CIRCUIT_BREAKER_OPEN, 
                String.format("Circuit breaker is open for bank: %s", bankName));
        } else if (e instanceof BulkheadFullException) {
            return ApiResponse.error(ResponseCode.BULKHEAD_FULL, 
                String.format("Too many concurrent requests for bank: %s", bankName));
        } else if (e instanceof RequestNotPermitted) {
            return ApiResponse.error(ResponseCode.RATE_LIMIT_EXCEEDED, 
                String.format("Rate limit exceeded for bank: %s", bankName));
        } else if (e instanceof TimeoutException) {
            return ApiResponse.error(ResponseCode.TIMEOUT_ERROR, 
                String.format("Operation timed out for bank: %s", bankName));
        }
        
        return ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, 
            String.format("Operation failed for bank: %s. Error: %s", bankName, e.getMessage()));
    }

    private CircuitBreaker getOrCreateCircuitBreaker(String bankName) {
        String backend;
        switch(bankName.toUpperCase()) {
            case "BANK_A":
                backend = BANK_A_BACKEND;
                break;
            case "BANK_B":
                backend = BANK_B_BACKEND;
                break;
            default:
                throw new IllegalArgumentException("Invalid bank name: " + bankName);
        }
        
        return circuitBreakerRegistry.circuitBreaker(backend);
    }

    public ResponseEntity<ApiResponse<String>> getCircuitBreakerStatus(String bankName) {
        try {
            CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(bankName);
            String status = String.format("Bank: %s, State: %s, Failure Rate: %.2f%%", 
                bankName,
                circuitBreaker.getState(),
                circuitBreaker.getMetrics().getFailureRate());
            return ApiResponse.success(status);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(ResponseCode.BAD_REQUEST, e.getMessage());
        }
    }
}