package com.example.demo.adapter.kafka.consumer;

import com.example.demo.dto.request.fraud.CheckFraudTransactionRequestDto;
import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.services.FraudService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import static com.example.demo.mapper.FraudMapper.parseMessage;


@Service
@Slf4j
public class AccountConsumer {

    private final FraudService fraudService;

    public AccountConsumer(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @KafkaListener(
            topics = "${spring.kafka.producer.topic.account.balance.reserved}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenAccountBalanceUpdated(ConsumerRecord<String, String> message, Acknowledgment ack) {
        CheckFraudTransactionRequestDto checkFraudTransactionRequestDto = parseMessage(message.value(), new TypeReference<CheckFraudTransactionRequestDto>() {});

        fraudService.fraudDetection(checkFraudTransactionRequestDto);

        log.info("complete update status: {}", checkFraudTransactionRequestDto.getTransactionId() );
        ack.acknowledge();
    }

}
