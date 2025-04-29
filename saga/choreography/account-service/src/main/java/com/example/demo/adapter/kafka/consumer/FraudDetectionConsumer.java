package com.example.demo.adapter.kafka.consumer;

import com.example.demo.dto.message.fraud.FraudTransactionDetectedMessageDto;
import com.example.demo.dto.message.fraud.FraudTransactionVerifiedMessageDto;
import com.example.demo.services.AccountService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import static com.example.demo.mapper.AccountMapper.parseMessage;

@Service
@Slf4j
public class FraudDetectionConsumer {
    private final AccountService accountService;

    public FraudDetectionConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.fraud.transaction.verified}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenTransactionFraudVerified(ConsumerRecord<String, String> message, Acknowledgment ack) {
        FraudTransactionVerifiedMessageDto transactionMessage = parseMessage(message.value(), new TypeReference<FraudTransactionVerifiedMessageDto>() {});

        accountService.completeTransaction(transactionMessage);

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.fraud.transaction.detected}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenTransactionFraudDetected(ConsumerRecord<String, String> message, Acknowledgment ack) {
        FraudTransactionDetectedMessageDto transactionMessage = parseMessage(message.value(), new TypeReference<FraudTransactionDetectedMessageDto>() {});

        accountService.rollbackTransaction(transactionMessage);

        ack.acknowledge();
    }
}
