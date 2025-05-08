package com.example.step3service.controller;

import com.example.step3service.service.Step3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/step3")
public class Step3Controller {

    private final Step3Service step3Service;

    public Step3Controller(Step3Service step3Service) {
        this.step3Service = step3Service;
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeStep3(@RequestBody String payload, @RequestParam(required = false) String transactionId) {
        String txId = (transactionId == null || transactionId.isEmpty()) ? UUID.randomUUID().toString() : transactionId;
        try {
            String result = step3Service.processStep3(txId, payload);
            return ResponseEntity.ok("Step 3 executed manually. Result: " + result + ", TX_ID: " + txId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Manual Step 3 execution failed for TX_ID: " + txId + ": " + e.getMessage());
        }
    }

    @PostMapping("/compensate")
    public ResponseEntity<String> compensateStep3(@RequestBody String payload, @RequestParam String transactionId) {
        try {
            step3Service.compensateStep3(transactionId, payload); // Payload here could be a reason string
            return ResponseEntity.ok("Step 3 compensation initiated for transaction ID: " + transactionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Step 3 compensation failed for TX_ID: " + transactionId + ": " + e.getMessage());
        }
    }
} 