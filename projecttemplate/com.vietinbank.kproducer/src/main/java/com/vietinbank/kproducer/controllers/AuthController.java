package com.vietinbank.kproducer.controllers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietinbank.kproducer.dto.request.user.LoginRequestDto;
import com.vietinbank.kproducer.dto.request.user.RegisterRequestDto;
import com.vietinbank.kproducer.dto.response.user.LoginResponseDto;
import com.vietinbank.kproducer.security.JwtUtil;
import com.vietinbank.kproducer.services.LoggingService;
import com.vietinbank.kproducer.services.UserService;

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
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        loggingService.logInfo("Bắt đầu xử lý đăng nhập cho user: " + request.getUsername());
        return userService.login(request);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok("Logged out successfully");
    }
}