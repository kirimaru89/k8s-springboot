package com.example.step1service.controller;

import com.example.step1service.service.Step1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/step1")
public class Step1Controller {

    private final Step1Service step1Service;

    public Step1Controller(Step1Service step1Service) {
        this.step1Service = step1Service;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startSaga(@RequestBody String payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            step1Service.processStep1(transactionId, payload);
            return ResponseEntity.ok("Step 1 initiated for transaction ID: " + transactionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to initiate Step 1: " + e.getMessage());
        }
    }

    @PostMapping("/execute") // Manual execution endpoint as per requirement.md
    public ResponseEntity<String> executeStep1(@RequestBody String payload) {
        String transactionId = UUID.randomUUID().toString();
        try {
            String result = step1Service.processStep1(transactionId, payload); // Assuming processStep1 can be called directly
            return ResponseEntity.ok("Step 1 executed manually. Result: " + result + ", TX_ID: " + transactionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Manual Step 1 execution failed: " + e.getMessage());
        }
    }

    @PostMapping("/compensate") // Manual compensation endpoint
    public ResponseEntity<String> compensateStep1(@RequestBody String transactionId) { // Assuming compensation needs a transactionId
        try {
            step1Service.compensateStep1(transactionId, "Manual compensation trigger");
            return ResponseEntity.ok("Step 1 compensation initiated for transaction ID: " + transactionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Step 1 compensation failed: " + e.getMessage());
        }
    }
} 