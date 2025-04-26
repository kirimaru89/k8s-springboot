package com.vietinbank.kconsumer.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Service
public class KafkaConsumerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String TOPIC = "kTopic";

    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String key = record.key() != null ? record.key() : "<null>";
        
        try {
            log.info("Received message from topic {}, partition {}, offset {}: {}",
                    record.topic(), record.partition(), record.offset(), record.value());
            
            // Process the message here
            // For now, just log it
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            log.info("Successfully processed message with key {}", key);
            
        } catch (Exception e) {
            log.error("Error processing message with key {}: {}", key, e.getMessage(), e);
            // Don't acknowledge - the message will be retried by the container's error handler
            throw e;
        }
    }
}