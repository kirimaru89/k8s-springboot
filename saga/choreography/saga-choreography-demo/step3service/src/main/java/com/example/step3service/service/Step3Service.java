package com.example.step3service.service;

import com.example.step3service.config.KafkaTopicConfig;
import com.example.step3service.dto.SagaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Step3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step3Service.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public Step3Service(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
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
    public String processStep3(String transactionId, String payloadFromStep2) {
        LOGGER.info("Processing Step 3 for transaction ID: {} with payload: {}", transactionId, payloadFromStep2);
        boolean success = !payloadFromStep2.contains("fail_step3");

        if (success) {
            String result = "Step 3 completed successfully for: " + payloadFromStep2;
            LOGGER.info(result);
            SagaEvent event = new SagaEvent(transactionId, result, "SUCCESS", "STEP3", null);
            sendMessage(KafkaTopicConfig.STEP3_COMPLETED_TOPIC, event);
            sendMessage(KafkaTopicConfig.SAGA_COMPLETED_TOPIC, event);
            return result;
        } else {
            String errorMsg = "Step 3 failed for: " + payloadFromStep2;
            LOGGER.error(errorMsg);
            SagaEvent event = new SagaEvent(transactionId, payloadFromStep2, "FAILURE", "STEP3", errorMsg);
            sendMessage(KafkaTopicConfig.STEP3_FAILED_TOPIC, event);
            sendMessage(KafkaTopicConfig.STEP3_FAILED_FOR_STEP2_COMPENSATION_TOPIC, event);
            sendMessage(KafkaTopicConfig.SAGA_FAILED_TOPIC, event);
            throw new RuntimeException(errorMsg);
        }
    }

    public void compensateStep3(String transactionId, String reason) {
        LOGGER.info("Compensating Step 3 for transaction ID: {}. Reason: {}", transactionId, reason);
        SagaEvent compensationEvent = new SagaEvent(transactionId, reason, "COMPENSATE_REQUESTED", "STEP3_COMPENSATION", "Step 3 compensating, requesting Step 2 compensation");
        sendMessage(KafkaTopicConfig.STEP3_FAILED_FOR_STEP2_COMPENSATION_TOPIC, compensationEvent);
        LOGGER.info("Reversing step3 for transaction {}", transactionId);
    }

    // --- Kafka Consumer Logic ---
    @KafkaListener(topics = KafkaTopicConfig.START_STEP3_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToStartStep3(String message) {
        LOGGER.info(String.format("Received message for START_STEP3_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Received event to start step 3 for transaction ID: {}", event.getTransactionId());
            processStep3(event.getTransactionId(), event.getPayload());
        } catch (Exception e) {
            LOGGER.error("Error processing start step 3 message: " + e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.COMPENSATE_STEP3_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToCompensateStep3(String message) {
        LOGGER.info(String.format("Received message for COMPENSATE_STEP3_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Received event to compensate step 3 for transaction ID: {}", event.getTransactionId());
            compensateStep3(event.getTransactionId(), event.getPayload());
        } catch (Exception e) {
            LOGGER.error("Error processing compensation message for step 3: " + e.getMessage(), e);
        }
    }
} 