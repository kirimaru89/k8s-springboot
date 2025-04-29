package com.example.demo.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.CompletableFuture;
import com.example.demo.dto.ApiResponse;
import com.example.demo.enums.ResponseCode;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.ResponseEntity;
import java.time.Duration;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

@Service
public class ExampleService {
//    Circuit Breaker: Sử dụng @CircuitBreaker để ngăn chặn các lỗi cascade. Khi có nhiều lỗi, circuit breaker sẽ mở và chuyển sang fallback.
//    Retry: @Retry cho phép thử lại các operation thất bại với số lần và thời gian chờ được cấu hình.
//    Bulkhead: @Bulkhead giới hạn số lượng concurrent calls để tránh quá tải.
//    Timeout: @TimeLimiter đảm bảo các operation không chạy quá lâu. Lưu ý phải trả về CompletableFuture.
//    Rate Limiting: @RateLimiter giới hạn số lượng request trong một khoảng thời gian.
//    Fallback: Các phương thức fallback được gọi khi có lỗi, với các handler riêng cho từng loại lỗi.

    private static final String BACKEND = "example";
    private static final String BANK_A_BACKEND = "bank-a";
    private static final String BANK_B_BACKEND = "bank-b";

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public ExampleService(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleException")
    @Retry(name = BACKEND, fallbackMethod = "handleException")
    public ResponseEntity<ApiResponse<String>> doSomething(boolean success) {
        if (!success) {
            throw new RuntimeException("Service failed!");
        }
        return ApiResponse.success("Operation completed successfully");
    }

    public ResponseEntity<ApiResponse<String>> getCircuitBreakerStatus() {
        String state = circuitBreakerRegistry.circuitBreaker(BACKEND).getState().toString();
        return ApiResponse.success(state);
    }
        
    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleException")
    @Retry(name = BACKEND, fallbackMethod = "handleException")
    @TimeLimiter(name = BACKEND, fallbackMethod = "handleException")
    @RateLimiter(name = BACKEND, fallbackMethod = "handleException")
    @Bulkhead(name = BACKEND)
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> serviceWithTimeout() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return ApiResponse.success("Delayed response");
        });
    }

    // Cập nhật fallback method để xử lý các loại exception cụ thể
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        Span span = Span.current();
        span.recordException(e);
        span.setStatus(StatusCode.ERROR, "There is an error");

        if (e instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            return ApiResponse.error(ResponseCode.CIRCUIT_BREAKER_OPEN);
        } 
        // else if (e instanceof RequestNotPermitted) {
        //     return ApiResponse.error(ResponseCode.RATE_LIMIT_EXCEEDED);
        // } else if (e instanceof BulkheadFullException) {
        //     return ApiResponse.error(ResponseCode.BULKHEAD_FULL);
        // } else if (e instanceof java.util.concurrent.TimeoutException) {
        //     return ApiResponse.error(ResponseCode.TIMEOUT_ERROR);
        // }
        
        // Mặc định trả về internal server error
        return ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }
}