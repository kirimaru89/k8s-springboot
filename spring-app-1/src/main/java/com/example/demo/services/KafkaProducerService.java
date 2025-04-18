package com.example.demo.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "app-communication";
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendMessage(String message) {
        Span currentSpan = Span.current();
        SpanContext context = currentSpan.getSpanContext();
        String traceId = null;
        if (context.isValid()) {
            traceId = context.getTraceId();
            String spanId = context.getSpanId();
            log.info("Current traceId={}, spanId={}", traceId, spanId);
            log.info("Sending message to Kafka [txId={}]: {}", traceId, message);
        }

        final String transactionId = (traceId != null) ? traceId : UUID.randomUUID().toString();
        try {
            kafkaTemplate.executeInTransaction(kt -> {
                try {
                    CompletableFuture<SendResult<String, String>> future = 
                        kt.send(TOPIC, transactionId, message);

                    SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);

                    log.info("Message sent to topic {} with offset {} [txId={}]", 
                            TOPIC, result.getRecordMetadata().offset(), transactionId);
                    
                    return true; // Return true to indicate success
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while sending to Kafka [txId={}]", transactionId, e);
                    throw new KafkaException("Interrupted during Kafka transaction", e);
                } catch (ExecutionException | TimeoutException e) {
                    log.error("Failed to send message [txId={}]: {}", transactionId, e.getMessage(), e);
                    throw new KafkaException("Failed to send message to Kafka", e);
                }
            });
        } catch (Exception ex) {
            log.error("Transaction failed [txId={}]: {}", 
                    context.isValid() ? context.getTraceId() : "unknown", ex.getMessage(), ex);
            throw new RuntimeException("Error in Kafka transaction", ex);
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