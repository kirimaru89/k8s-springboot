package com.example.demo.services;

import com.example.demo.adapter.kafka.producer.KafkaProducer;
import com.example.demo.dto.message.fraud.FraudTransactionDetectedMessageDto;
import com.example.demo.dto.message.fraud.FraudTransactionVerifiedMessageDto;
import com.example.demo.dto.request.fraud.CheckFraudTransactionRequestDto;
import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.models.FraudDetection;
import com.example.demo.repositories.FraudDetectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


import static com.example.demo.mapper.FraudMapper.*;

@Slf4j
@Service
public class FraudService {
    @Value("${spring.kafka.producer.topic.fraud.transaction.verified}")
    private String fraudTransactionVerifiedTopic ;
    @Value("${spring.kafka.producer.topic.fraud.transaction.detected}")
    private String fraudTransactionDetectedTopic ;

    private final FraudDetectionRepository fraudDetectionRepository;
    private final TransactionTemplate txTemplate;
    private final KafkaProducer kafkaProducer;

    public FraudService(
            FraudDetectionRepository fraudDetectionRepository,
            PlatformTransactionManager transactionManager,
            KafkaProducer kafkaProducer) {
        this.fraudDetectionRepository = fraudDetectionRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.kafkaProducer = kafkaProducer;
    }


    public CheckFraudTransactionResponseDto fraudDetection(CheckFraudTransactionRequestDto transactionMessage) {
        return txTemplate.execute(status -> {
            FraudDetection fraudDetection = new FraudDetection();
            fraudDetection.setTransactionId(transactionMessage.getTransactionId());

            if (transactionMessage.getAmount() > 10000) {
                String reason = "Error: fraud detected";
                fraudDetection.setIsFraudulent(true);
                fraudDetection.setReason(reason);
                fraudDetectionRepository.save(fraudDetection);

                sendFraudTransactionDetected(transactionMessage.getTransactionId(), reason);
            } else {
                fraudDetection.setIsFraudulent(false);
                fraudDetectionRepository.save(fraudDetection);

                sendFraudTransactionVerified(transactionMessage.getTransactionId());
            }

            return toResponseDto(fraudDetection);
        });
    }

    private void sendFraudTransactionVerified(Long transactionId) {
        String messageKey = transactionId.toString();
        FraudTransactionVerifiedMessageDto fraudTransactionVerifiedMessageDto = new FraudTransactionVerifiedMessageDto();
        fraudTransactionVerifiedMessageDto.setTransactionId(transactionId);

        String messageValue = toMessage(fraudTransactionVerifiedMessageDto);
        kafkaProducer.sendMessage(fraudTransactionVerifiedTopic, messageKey, messageValue);
    }

    private void sendFraudTransactionDetected(Long transactionId, String reason) {
        String messageKey = transactionId.toString();
        FraudTransactionDetectedMessageDto fraudTransactionDetectedMessageDto = new FraudTransactionDetectedMessageDto ();
        fraudTransactionDetectedMessageDto.setTransactionId(transactionId);
        fraudTransactionDetectedMessageDto.setReason(reason);

        String messageValue = toMessage(fraudTransactionDetectedMessageDto);
        kafkaProducer.sendMessage(fraudTransactionDetectedTopic, messageKey, messageValue);
    }

} 