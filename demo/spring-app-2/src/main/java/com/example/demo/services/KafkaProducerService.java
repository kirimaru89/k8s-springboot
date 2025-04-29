package com.example.demo.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaProducerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "app-communication";
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    /**
     * Send message to Kafka with circuit breaker protection and error handling
     */
    @CircuitBreaker(name = "kafkaCircuitBreaker", fallbackMethod = "sendMessageFallback")
    public void sendMessage(String message) {
        String transactionId = UUID.randomUUID().toString();
        log.info("Sending message to Kafka [txId={}]: {}", transactionId, message);
        
        try {
            // Add transaction ID to the message or headers for end-to-end tracing
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC, transactionId, message);
                
            // Set timeout for the send operation
            SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);
            
            log.info("Message sent to topic {} with offset {} [txId={}]", 
                TOPIC, result.getRecordMetadata().offset(), transactionId);
                
        } catch (org.apache.kafka.common.errors.TimeoutException | java.util.concurrent.TimeoutException ex) {
            log.error("Kafka broker timeout [txId={}]: {}", transactionId, ex.getMessage(), ex);
            throw new RuntimeException("Kafka broker timeout", ex);
        } catch (org.apache.kafka.common.errors.NotLeaderOrFollowerException ex) {
            log.error("Kafka partition unavailable [txId={}]: {}", transactionId, ex.getMessage(), ex);
            throw new RuntimeException("Kafka partition unavailable", ex);
        } catch (Exception ex) {
            log.error("Failed to send message to Kafka [txId={}]: {}", 
                transactionId, ex.getMessage(), ex);
            throw new RuntimeException("Error sending message to Kafka", ex);
        }
    }
    
    /**
     * Fallback method when circuit breaker is open
     */
    public void sendMessageFallback(String message, Exception ex) {
        String transactionId = UUID.randomUUID().toString();
        log.warn("Circuit breaker open - unable to send message to Kafka [txId={}]: {}", 
            transactionId, ex.getMessage());
            
        // Here you could implement fallback strategies:
        // 1. Store in a local queue/database for later retry
        // 2. Send alert/notification about failed send
        // 3. Use an alternative messaging system if available
    }
}