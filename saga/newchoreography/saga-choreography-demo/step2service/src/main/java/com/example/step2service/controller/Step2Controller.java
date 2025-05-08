package com.example.step2service.controller;

import com.example.step2service.service.Step2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/step2")
public class Step2Controller {

    private final Step2Service step2Service;

    public Step2Controller(Step2Service step2Service) {
        this.step2Service = step2Service;
    }

    // Manual endpoint to execute step 2 - useful for testing
    @PostMapping("/execute")
    public ResponseEntity<String> executeStep2(@RequestBody String payload, @RequestParam(required = false) String transactionId) {
        String txId = (transactionId == null || transactionId.isEmpty()) ? UUID.randomUUID().toString() : transactionId;
        try {
            String result = step2Service.processStep2(txId, payload);
            return ResponseEntity.ok("Step 2 executed manually. Result: " + result + ", TX_ID: " + txId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Manual Step 2 execution failed for TX_ID: " + txId + ": " + e.getMessage());
        }
    }

    // Manual endpoint to compensate step 2
    @PostMapping("/compensate")
    public ResponseEntity<String> compensateStep2(@RequestBody String payload, @RequestParam String transactionId) {
        try {
            step2Service.compensateStep2(transactionId, payload); // Payload here could be a reason string
            return ResponseEntity.ok("Step 2 compensation initiated for transaction ID: " + transactionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Step 2 compensation failed for TX_ID: " + transactionId + ": " + e.getMessage());
        }
    }
} 