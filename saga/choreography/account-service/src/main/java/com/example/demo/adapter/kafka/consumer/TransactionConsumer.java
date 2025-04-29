package com.example.demo.adapter.kafka.consumer;

import com.example.demo.dto.message.TransactionInitiatedMessageDto;
import com.example.demo.services.AccountService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import static com.example.demo.mapper.AccountMapper.*;


@Service
@Slf4j
public class TransactionConsumer {
    private final AccountService accountService;

    public TransactionConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.transaction.initiated}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenTransactionInitiated(ConsumerRecord<String, String> message, Acknowledgment ack) {
        TransactionInitiatedMessageDto transactionMessage = parseMessage(message.value(), new TypeReference<TransactionInitiatedMessageDto>() {});

        accountService.transaction(transactionMessage);
        log.info("Received key success: {}, message: {}", message.key(), message.value());

        ack.acknowledge();
    }
}
