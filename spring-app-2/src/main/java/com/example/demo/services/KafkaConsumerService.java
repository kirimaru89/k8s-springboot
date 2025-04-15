package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.ScopedSpan;
import com.example.demo.models.Artist;
import com.example.demo.repositories.ArtistRepository;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KafkaConsumerService {
    @Autowired
    private ArtistRepository artistRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String TOPIC = "app-communication";
    private static final AtomicInteger messageCounter = new AtomicInteger(0);

    @Autowired
    private Tracer tracer;
    
    /**
     * Process messages from Kafka with error handling and retry logic
     * Uses manual acknowledgment to ensure proper error handling
     */
    @KafkaListener(
        topics = TOPIC, 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @CircuitBreaker(name = "kafkaCircuitBreaker", fallbackMethod = "processFallback")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        // Generate a unique transaction ID for tracking this message processing
        String transactionId = UUID.randomUUID().toString();
        int count = messageCounter.incrementAndGet();
        
        // Create and start a scoped span (automatically placed on the current thread)
        ScopedSpan span = tracer.startScopedSpan("process-kafka-message");
        try {
            // Add tags/attributes to the span for observability
            span.tag("message.type", "kafka");
            span.tag("kafka.topic", topic);
            span.tag("kafka.partition", String.valueOf(partition));
            span.tag("kafka.offset", String.valueOf(offset));
            span.tag("transaction.id", transactionId);
            
            log.info("Processing message [txId={}] from topic {}, partition {}, offset {}: {}", 
                    transactionId, topic, partition, offset, message);
            
            // Process the message (made idempotent)
            processTransactionIdempotently(message, transactionId);
            
            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            
            log.info("Successfully processed message [txId={}]", transactionId);
        } catch (Exception e) {
            span.error(e);
            log.error("Error processing message [txId={}]: {}", transactionId, e.getMessage(), e);
            
            // Don't acknowledge - the message will be retried by the container's error handler
            // The error handler will eventually send to DLT after retries are exhausted
            
            // Re-throw exception to let the error handler deal with it
            throw e;
        } finally {
            // Always close the span
            span.end();
        }
    }
    
    /**
     * Fallback method when circuit breaker is open
     */
    public void processFallback(
            String message,
            String topic,
            int partition,
            long offset,
            Acknowledgment acknowledgment,
            Exception e
    ) {
        String transactionId = UUID.randomUUID().toString();
        log.warn("Circuit breaker open - falling back for message [txId={}]: {}", transactionId, e.getMessage());
        
        // Always acknowledge in the fallback to prevent retries when the circuit is open
        acknowledgment.acknowledge();
        
        // Here you could implement alternative processing:
        // - Store in a local database for later processing
        // - Send alert/notification about unprocessed message
        // - etc.
    }
    
    /**
     * Process the transaction in an idempotent way
     * This ensures we can safely retry processing without duplicating effects
     */
    private void processTransactionIdempotently(String message, String transactionId) {
        // In a real implementation, you would:
        // 1. Check if this transaction has already been processed (using Redis, DB, etc.)
        // 2. If already processed, return success without processing again
        // 3. If not processed, do the processing and mark as processed
        
        var artists = artistRepository.findAll();
        log.info("Processing message [txId={}]:", transactionId);
        for (Artist artist : artists) {
            log.info("Processing artist [txId={}]: {}", transactionId, artist.getName());
        }
        
        // Simulated check for message format validation
        if (message != null && message.contains("error_simulation")) {
            throw new IllegalArgumentException("Error processing message: Invalid format");
        }
    }
}