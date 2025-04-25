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

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.ScopedSpan;

import com.vietinbank.kconsumer.models.Artist;
import com.vietinbank.kconsumer.repositories.ArtistRepository;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@Service
public class KafkaConsumerService {
    @Autowired
    private ArtistRepository artistRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String TOPIC = "app-communication";

    @Autowired
    private Tracer tracer;

    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String transactionId = record.key() != null ? record.key() : "<null>";

        try {
            log.info("-----Processing message [txId={}] from topic {}, partition {}, offset {}: {}",
                    transactionId, record.topic(), record.partition(), record.offset(), record.value());

            processTransactionIdempotently(transactionId, record.value(), record.topic(), record.partition(), record.offset(), acknowledgment);

            acknowledgment.acknowledge();
            log.info("✅ Successfully processed message [txId={}]", transactionId);

        } catch (Exception e) {
            log.error("❌ Error processing message [txId={}, message={}]: {}", transactionId, record.value(), e.getMessage(), e);
            throw e;
        }
    }
    
    // @KafkaListener(
    //     topics = TOPIC, 
    //     groupId = "${spring.kafka.consumer.group-id}"
    // )
    // public void listen(
    //         @Header(KafkaHeaders.RECEIVED_KEY) String transactionId,  // ✅ from Kafka key
    //         @Payload String message,
    //         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
    //         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
    //         @Header(KafkaHeaders.OFFSET) long offset,
    //         Acknowledgment acknowledgment
    // ) {
    //     try {
    //         log.info("Processing message [txId={}] from topic {}, partition {}, offset {}: {}", 
    //                 transactionId, topic, partition, offset, message);

    //         // ✅ Now use the correct transactionId from producer
    //         processTransactionIdempotently(transactionId, message, topic, partition, offset, acknowledgment);

    //         acknowledgment.acknowledge();
    //         log.info("Successfully processed message [txId={}]", transactionId);

    //     } catch (Exception e) {
    //         log.error("Error processing message [txId={}]: {}", transactionId, e.getMessage(), e);

    //         // Don't acknowledge - the message will be retried by the container's error handler
    //         // The error handler will eventually send to DLT after retries are exhausted
            
    //         // Re-throw exception to let the error handler deal with it
    //         throw e;
    //     }
    // }

    private void processTransactionIdempotently(String transactionId, String message, String topic, int partition, long offset, Acknowledgment acknowledgment) {
        if (isMessageProcessed(transactionId)) {
            log.info("Message [txId={}] already processed, skipping", transactionId);
            return;
        }

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

    private boolean isMessageProcessed(String transactionId) {
        // In a real implementation, you would:
        // 1. Check if this transaction has already been processed (using Redis, DB, etc.)
        // 2. If already processed, return success without processing again
        // 3. If not processed, do the processing and mark as processed
        return false;
    }

    // Custom exception classes
    public static class InvalidMessageFormatException extends RuntimeException {
        private final String transactionId;
        
        public InvalidMessageFormatException(String message, String transactionId) {
            super(message);
            this.transactionId = transactionId;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
    }

    public static class DownstreamServiceException extends RuntimeException {
        private final String transactionId;
        
        public DownstreamServiceException(String message, String transactionId) {
            super(message);
            this.transactionId = transactionId;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
    }
}