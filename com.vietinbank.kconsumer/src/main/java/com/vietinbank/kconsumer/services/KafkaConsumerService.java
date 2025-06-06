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
    public void listen(ConsumerRecord<String, String> record) {
        String transactionId = record.key() != null ? record.key() : "<null>";

        try {
            log.info("Received message -> topic={}, partition={}, offset={}, key={}, value={}",
                    record.topic(), record.partition(), record.offset(), transactionId, record.value());

            processTransactionIdempotently(transactionId, record.value());

            log.info("Successfully processed message with key {}", transactionId);

        } catch (Exception e) {
            log.error("Error processing message. topic={}, partition={}, offset={}, key={}, error={}",
                    record.topic(), record.partition(), record.offset(), transactionId, e.getMessage(), e);
            throw e; // container will retry
        }
    }

    private void processTransactionIdempotently(String transactionId, String message) {
        if (isMessageProcessed(transactionId)) {
            log.info("Message [txId={}] already processed, skipping", transactionId);
            return;
        }

        // log.info("Processing message: {}", message);
    }

    private boolean isMessageProcessed(String transactionId) {
        // TODO: Implement this
        return false;
    }
}