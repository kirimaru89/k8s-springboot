package com.vietinbank.kconsumer.controllers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietinbank.kconsumer.dto.request.user.LoginRequestDto;
import com.vietinbank.kconsumer.dto.request.user.RegisterRequestDto;
import com.vietinbank.kconsumer.security.JwtUtil;
import com.vietinbank.kconsumer.services.LoggingService;
import com.vietinbank.kconsumer.services.UserService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private LoggingService loggingService;

    @Autowired
    public AuthController() {
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDto request) {
        loggingService.logInfo("Bắt đầu xử lý đăng nhập cho user: " + request.getUsername());
        
        // Simulate login processing
        String token = jwtUtil.generateToken(request.getUsername());
                
        loggingService.logInfo("Đã tạo token cho user: " + request.getUsername());
        return token;
    }
    
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {
        return "Logged out successfully";
    }
}