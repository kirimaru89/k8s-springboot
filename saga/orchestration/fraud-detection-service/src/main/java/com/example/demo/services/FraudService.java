package com.example.demo.services;

import com.example.demo.dto.request.fraud.CheckFraudTransactionRequestDto;
import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.models.FraudDetection;
import com.example.demo.repositories.FraudDetectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


import static com.example.demo.mapper.FraudMapper.*;

@Slf4j
@Service
public class FraudService {

    private final FraudDetectionRepository fraudDetectionRepository;
    private final TransactionTemplate txTemplate;

    public FraudService(
            FraudDetectionRepository fraudDetectionRepository,
            PlatformTransactionManager transactionManager) {
        this.fraudDetectionRepository = fraudDetectionRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
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
            } else {
                fraudDetection.setIsFraudulent(false);
                fraudDetectionRepository.save(fraudDetection);
            }

            return toResponseDto(fraudDetection);
        });
    }

} 