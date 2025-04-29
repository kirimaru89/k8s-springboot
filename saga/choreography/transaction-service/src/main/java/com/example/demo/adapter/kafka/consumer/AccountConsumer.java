package com.example.demo.adapter.kafka.consumer;

import com.example.demo.dto.request.transaction.UpdateTransactionStatusDto;
import com.example.demo.services.TransactionService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import static com.example.demo.mapper.TransactionMapper.*;


@Service
@Slf4j
public class AccountConsumer {

    private final TransactionService transactionService;
    public AccountConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.account.balance.completed}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenAccountBalanceUpdated(ConsumerRecord<String, String> message, Acknowledgment ack) {
        UpdateTransactionStatusDto transactionCompletedMessageDto = parseMessage(message.value(), new TypeReference<UpdateTransactionStatusDto>() {});

        transactionService.completeUpdateStatus(transactionCompletedMessageDto.getTransactionId());

        log.info("complete update status: {}", transactionCompletedMessageDto.getTransactionId() );
        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.account.balance.rollback}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenAccountBalanceFailed(ConsumerRecord<String, String> message, Acknowledgment ack) {
        UpdateTransactionStatusDto transactionFailedMessageDto = parseMessage(message.value(), new TypeReference<UpdateTransactionStatusDto>() {});

        transactionService.rollbackUpdateStatus(transactionFailedMessageDto.getTransactionId());

        log.info("rollback update status: {}", transactionFailedMessageDto.getTransactionId() );
        ack.acknowledge();
    }

}
