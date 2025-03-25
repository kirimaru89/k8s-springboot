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

@Service
public class ExampleService {
//    Circuit Breaker: Sử dụng @CircuitBreaker để ngăn chặn các lỗi cascade. Khi có nhiều lỗi, circuit breaker sẽ mở và chuyển sang fallback.
//    Retry: @Retry cho phép thử lại các operation thất bại với số lần và thời gian chờ được cấu hình.
//    Bulkhead: @Bulkhead giới hạn số lượng concurrent calls để tránh quá tải.
//    Timeout: @TimeLimiter đảm bảo các operation không chạy quá lâu. Lưu ý phải trả về CompletableFuture.
//    Rate Limiting: @RateLimiter giới hạn số lượng request trong một khoảng thời gian.
//    Fallback: Các phương thức fallback được gọi khi có lỗi, với các handler riêng cho từng loại lỗi.
    
    private static final String BACKEND = "example";
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public ExampleService(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    
    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleGeneralException")
    @Retry(name = BACKEND, fallbackMethod = "handleGeneralException")
    @RateLimiter(name = BACKEND, fallbackMethod = "handleRateLimitException")
    @Bulkhead(name = BACKEND, fallbackMethod = "handleBulkheadException")
    public String doSomething() {
        // Giả lập một service call có thể bị lỗi
        if(Math.random() < 0.7) { // 70% xác suất gây lỗi
            throw new RuntimeException("Service failed!");
        }
        return "Success";
    }
    
    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleGeneralException")
    @Retry(name = BACKEND, fallbackMethod = "handleGeneralException")
    @RateLimiter(name = BACKEND, fallbackMethod = "handleRateLimitException")
    @Bulkhead(name = BACKEND, fallbackMethod = "handleBulkheadException")
    public String serviceWithSuccessfulResponse() {
        // Bỏ random error, luôn trả về success
        return "Success response";
    }

    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleGeneralException")
    @Retry(name = BACKEND, fallbackMethod = "handleGeneralException")
    @RateLimiter(name = BACKEND, fallbackMethod = "handleRateLimitException")
    @Bulkhead(name = BACKEND, fallbackMethod = "handleBulkheadException")
    public String serviceWithFailureResponse() {
        // Luôn throw exception để test circuit breaker
        throw new RuntimeException("Service failed!");
    }

    @CircuitBreaker(name = BACKEND, fallbackMethod = "handleTimeout")
    @Retry(name = BACKEND, fallbackMethod = "handleTimeout")
    @TimeLimiter(name = BACKEND)
    @RateLimiter(name = BACKEND)
    @Bulkhead(name = BACKEND)
    public CompletableFuture<String> serviceWithTimeout() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000); // Simulate a 5-second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Delayed response";
        });
    }

    public String getCircuitBreakerStatus() {
        return circuitBreakerRegistry.circuitBreaker(BACKEND)
                .getState()
                .toString();
    }

    // Xử lý lỗi Bulkhead (quá tải)
    public CompletableFuture<String> handleBulkheadException(Exception e, io.github.resilience4j.bulkhead.BulkheadFullException b) {
        return CompletableFuture.completedFuture("Hệ thống đang quá tải, vui lòng thử lại sau");
    }

    // Xử lý lỗi Rate Limit
    public CompletableFuture<String> handleRateLimitException(Exception e, io.github.resilience4j.ratelimiter.RequestNotPermitted r) {
        return CompletableFuture.completedFuture("Đã vượt quá số lượng yêu cầu cho phép, vui lòng thử lại sau");
    }

    // Xử lý lỗi Timeout
    public CompletableFuture<String> handleTimeout(Exception e) {
        return CompletableFuture.completedFuture("Yêu cầu đã hết thời gian chờ");
    }

    // Xử lý các lỗi khác
    public CompletableFuture<String> handleGeneralException(Exception e) {
        return CompletableFuture.completedFuture("Đã xảy ra lỗi: " + e.getMessage());
    }
} 