package com.vietinbank.paymenthub.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Service
public class DeadLetterService {
    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);
    private static final String DLT_TOPIC = "dlt.app-communication";
    
    /**
     * Listen to all messages in the Dead Letter Topic
     * This provides visibility and monitoring of failed messages
     */
    @KafkaListener(
        topics = DLT_TOPIC, 
        groupId = "${spring.application.name}-dlt-group"
    )
    public void processDltMessages(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.EXCEPTION_FQCN, required = false) String exception,
            @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage
    ) {
        log.error("Dead letter message received: Topic={}, Partition={}, Offset={}, Exception={}, Message={}", 
                topic, partition, offset, exception, exceptionMessage);
        
        // Log the failed message for manual inspection
        log.error("Failed message content: {}", message);
        
        // Here you could implement:
        // 1. Store messages in a database for later analysis
        // 2. Send notifications to support team
        // 3. Implement manual retry mechanics for ops teams
    }
}