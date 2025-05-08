package com.example.step2service.service;

import com.example.step2service.config.KafkaTopicConfig;
import com.example.step2service.dto.SagaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Step2Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step2Service.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public Step2Service(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // --- Kafka Producer Logic ---
    private void sendMessage(String topic, SagaEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            LOGGER.info(String.format("Producing message -> %s to topic -> %s", message, topic));
            kafkaTemplate.send(topic, event.getTransactionId(), message);
        } catch (Exception e) {
            LOGGER.error(String.format("Error producing message to topic %s: %s", topic, e.getMessage()), e);
        }
    }

    // --- Business Logic ---
    public String processStep2(String transactionId, String payloadFromStep1) {
        LOGGER.info("Processing Step 2 for transaction ID: {} with payload: {}", transactionId, payloadFromStep1);
        boolean success = !payloadFromStep1.contains("fail_step2");

        if (success) {
            String result = "Step 2 completed successfully for: " + payloadFromStep1;
            LOGGER.info(result);
            SagaEvent event = new SagaEvent(transactionId, result, "SUCCESS", "STEP2", null);
            sendMessage(KafkaTopicConfig.STEP2_COMPLETED_TOPIC, event);
            sendMessage(KafkaTopicConfig.START_STEP3_TOPIC, event);
            return result;
        } else {
            String errorMsg = "Step 2 failed for: " + payloadFromStep1;
            LOGGER.error(errorMsg);
            SagaEvent event = new SagaEvent(transactionId, payloadFromStep1, "FAILURE", "STEP2", errorMsg);
            sendMessage(KafkaTopicConfig.STEP2_FAILED_TOPIC, event);
            sendMessage(KafkaTopicConfig.STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC, event);
            throw new RuntimeException(errorMsg);
        }
    }

    public void compensateStep2(String transactionId, String reason) {
        LOGGER.info("Compensating Step 2 for transaction ID: {}. Reason: {}", transactionId, reason);
        SagaEvent compensationEvent = new SagaEvent(transactionId, reason, "COMPENSATE_REQUESTED", "STEP2_COMPENSATION", "Step 2 compensating, requesting Step 1 compensation");
        sendMessage(KafkaTopicConfig.STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC, compensationEvent);
        LOGGER.info("Reversing step2 for transaction {}", transactionId);
    }

    // --- Kafka Consumer Logic ---
    @KafkaListener(topics = KafkaTopicConfig.START_STEP2_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToStartStep2(String message) {
        LOGGER.info(String.format("Received message for START_STEP2_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Received event to start step 2 for transaction ID: {}", event.getTransactionId());
            processStep2(event.getTransactionId(), event.getPayload());
        } catch (Exception e) {
            LOGGER.error("Error processing start step 2 message: " + e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.COMPENSATE_STEP2_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToCompensateStep2(String message) {
        LOGGER.info(String.format("Received message for COMPENSATE_STEP2_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Received event to compensate step 2 for transaction ID: {}", event.getTransactionId());
            compensateStep2(event.getTransactionId(), event.getPayload());
        } catch (Exception e) {
            LOGGER.error("Error processing compensation message for step 2: " + e.getMessage(), e);
        }
    }
} 