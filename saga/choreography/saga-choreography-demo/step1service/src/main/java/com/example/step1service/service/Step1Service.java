package com.example.step1service.service;

import com.example.step1service.config.KafkaTopicConfig;
import com.example.step1service.dto.SagaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate; // If WebClient/RestTemplate is used for compensation

@Service
public class Step1Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step1Service.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    // private final WebClient webClient; // Inject if using WebClient for compensation calls

    public Step1Service(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        // this.webClient = WebClient.builder().baseUrl("http://step2service:8082/api/step2").build(); // Example
    }

    // --- Kafka Producer Logic (moved from KafkaProducerService) ---
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
    public String processStep1(String transactionId, String payload) {
        LOGGER.info("Processing Step 1 for transaction ID: {} with payload: {}", transactionId, payload);

        // Simulate processing
        // In a real scenario, this could involve database operations, external API calls, etc.
        boolean success = !payload.equalsIgnoreCase("fail_step1"); // Simulate failure

        if (success) {
            String result = "Step 1 completed successfully for: " + payload;
            LOGGER.info(result);
            SagaEvent event = new SagaEvent(transactionId, payload, "SUCCESS", "STEP1", null);
            sendMessage(KafkaTopicConfig.STEP1_COMPLETED_TOPIC, event);
            // Also send a message to start step 2
            sendMessage(KafkaTopicConfig.START_STEP2_TOPIC, event);
            return result;
        } else {
            String errorMsg = "Step 1 failed for: " + payload;
            LOGGER.error(errorMsg);
            SagaEvent event = new SagaEvent(transactionId, payload, "FAILURE", "STEP1", errorMsg);
            sendMessage(KafkaTopicConfig.STEP1_FAILED_TOPIC, event);
            // Potentially trigger broader saga failure notification if step 1 is critical and fails immediately
            // sendMessage(KafkaTopicConfig.SAGA_FAILED_TOPIC, event);
            throw new RuntimeException(errorMsg);
        }
    }

    public void compensateStep1(String transactionId, String originalPayloadOrReason) {
        LOGGER.info("Compensating Step 1 for transaction ID: {}. Reason/Original Payload: {}", transactionId, originalPayloadOrReason);
        // Implement compensation logic for Step 1
        // e.g., revert database changes, call a compensation API
        // For this demo, we'll just log it.

        // Example: if compensation requires notifying other services or updating status
        SagaEvent compensationEvent = new SagaEvent(transactionId, originalPayloadOrReason, "COMPENSATED", "STEP1", "Step 1 compensated due to downstream failure or manual trigger");
        // sendMessage(KafkaTopicConfig.SAGA_FAILED_TOPIC, compensationEvent); // or a specific compensation topic

        LOGGER.info("Reversing step1 for transaction {}", transactionId);
    }

    // --- Kafka Consumer Logic (moved from KafkaConsumerService) ---
    @KafkaListener(topics = KafkaTopicConfig.COMPENSATE_STEP1_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToCompensateStep1(String message) {
        LOGGER.info(String.format("Received message for COMPENSATE_STEP1_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Received event to compensate step 1 for transaction ID: {}", event.getTransactionId());
            compensateStep1(event.getTransactionId(), event.getPayload());
        } catch (Exception e) {
            LOGGER.error("Error processing compensation message for step 1: " + e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenToStep2Failure(String message) {
        LOGGER.info(String.format("Received message for STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC: %s", message));
        try {
            SagaEvent event = objectMapper.readValue(message, SagaEvent.class);
            LOGGER.info("Step 2 failed, initiating compensation for Step 1. Transaction ID: {}", event.getTransactionId());
            compensateStep1(event.getTransactionId(), "Compensation due to Step 2 failure: " + event.getErrorMessage());
        } catch (Exception e) {
            LOGGER.error("Error processing Step 2 failure message for Step 1 compensation: " + e.getMessage(), e);
        }
    }
} 