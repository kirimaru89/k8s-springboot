package com.example.demo.controllers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.security.JwtUtil;
import com.example.demo.services.App1Service;
import com.example.demo.services.LoggingService;
import com.example.demo.services.UserService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private VaultTemplate vaultTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private App1Service app1Service;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private LoggingService loggingService;

    private final MeterRegistry meterRegistry;
    
    // COUNTER - Increases monotonically
    private final Counter loginAttemptsCounter;
    private final Counter failedLoginCounter;
    
    // GAUGE - Can go up and down, tracks current value
    private final AtomicInteger activeLoginSessions = new AtomicInteger(0);
    
    // HISTOGRAM (implemented via Timer) - Measures distribution of values
    private final Timer loginResponseTimeTimer;
    
    @Autowired
    public AuthController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // COUNTER examples
        this.loginAttemptsCounter = Counter.builder("auth.login.attempts.total")
            .description("Total number of login attempts")
            .register(meterRegistry);
            
        this.failedLoginCounter = Counter.builder("auth.login.failed.total")
            .description("Total number of failed login attempts")
            .register(meterRegistry);
            
        // GAUGE example - tracks current active sessions
        Gauge.builder("auth.sessions.active", activeLoginSessions, AtomicInteger::get)
            .description("Number of currently active login sessions")
            .register(meterRegistry);
            
        // HISTOGRAM example (via Timer with histogram statistics enabled)
        this.loginResponseTimeTimer = Timer.builder("auth.login.duration")
            .description("Distribution of login request processing times")
            .publishPercentiles(0.5, 0.95, 0.99)  // Publish 50th, 95th, 99th percentiles
            .publishPercentileHistogram()         // Enable histogram
            .sla(
                Duration.ofMillis(10),  // 10ms SLO bucket
                Duration.ofMillis(50),  // 50ms SLO bucket
                Duration.ofMillis(100)  // 100ms SLO bucket
            )
            .register(meterRegistry);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        // COUNTER - Increment for each registration attempt
        meterRegistry.counter("auth.register.attempts.total").increment();
        
        String result = userService.register(request);
        
        // COUNTER with tags - for successful registrations
        meterRegistry.counter("auth.register.outcomes", 
            "success", result.contains("success") ? "true" : "false").increment();
        
        return result;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // VaultResponse response = vaultTemplate.read("secret/data/spring-app-5");
        // loggingService.logInfo("🧪 Raw Vault Response: " + response.getData());

        // COUNTER - Track total login attempts 
        loginAttemptsCounter.increment();
        
        loggingService.logInfo("Bắt đầu xử lý đăng nhập cho user: " + request.getUsername());
        
        // HISTOGRAM (via Timer) - Measure login response time
        return loginResponseTimeTimer.record(() -> {
            try {
                // Simulate login processing
                String token = jwtUtil.generateToken(request.getUsername());
                
                // GAUGE - Increment active sessions on success
                activeLoginSessions.incrementAndGet();
                
                loggingService.logInfo("Đã tạo token cho user: " + request.getUsername());
                return token;
            } catch (Exception e) {
                // COUNTER - Track failed logins
                failedLoginCounter.increment();
                throw e;
            }
        });
    }
    
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {
        // GAUGE - Decrement active sessions on logout
        activeLoginSessions.decrementAndGet();
        
        return "Logged out successfully";
    }
}