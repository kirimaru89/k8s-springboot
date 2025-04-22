package com.example.demo.controllers;

import com.example.demo.services.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.demo.services.KafkaProducerService;
import com.example.demo.services.KafkaRequestService;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    @Autowired
    private KafkaRequestService requestService;

    @Autowired
    private LoggingService loggingService;
    
    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<String>> sendMessage(@RequestBody String message) {
        loggingService.logInfo("Received request to send message via Kafka: " + message);
        return CompletableFuture.supplyAsync(() -> {
            kafkaProducerService.sendMessage(message);
            return ResponseEntity.ok("Message sent to Kafka!");
        }).exceptionally(ex -> {
            log.error("Error sending message", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error sending message: " + ex.getMessage());
        });
    }

    @PostMapping("/request")
    public CompletableFuture<ResponseEntity<String>> sendRequest(@RequestBody String request) {
        return requestService.sendRequestAndGetReplyAsync(request)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> {
                log.error("Error processing request", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + ex.getMessage());
            });
    }
}