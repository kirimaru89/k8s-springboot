package com.example.demo.controllers;

import com.example.demo.services.LoggingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.KafkaProducerService;
import com.example.demo.services.KafkaRequestService;
import com.example.demo.services.LoggingService;
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
    public String sendMessage(@RequestBody String message) {
        loggingService.logInfo("Received request to send message via Kafka: {}");
        kafkaProducerService.sendMessage(message);
        return "Message sent to Kafka!";
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody String request) {
        try {
            String reply = requestService.sendRequestAndGetReply(request);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ResponseEntity.internalServerError()
                .body("Error processing request: " + e.getMessage());
        }
    }
}