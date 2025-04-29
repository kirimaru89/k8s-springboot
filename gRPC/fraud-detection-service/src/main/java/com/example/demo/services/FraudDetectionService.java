package com.example.demo.services;

import com.example.demo.models.FraudDetection;
import com.example.demo.repositories.FraudDetectionRepository;
import digitaldocuments.library.v1.FraudDetectionMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


import static com.example.demo.mapper.FraudMapper.*;

@Slf4j
@Service
public class FraudDetectionService {
    private final FraudDetectionRepository fraudDetectionRepository;
    private final TransactionTemplate txTemplate;

    public FraudDetectionService(
            FraudDetectionRepository fraudDetectionRepository,
            PlatformTransactionManager transactionManager) {
        this.fraudDetectionRepository = fraudDetectionRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    public FraudDetectionMessages.FraudDetection createFraudDetection(FraudDetectionMessages.CreateFraudDetectionRequest requestDto) {
        return txTemplate.execute(status -> {
            FraudDetection fraudDetection = fraudDetectionRepository.findByTransactionId(requestDto.getTransactionId()).orElse(null);
            if (fraudDetection != null) {
                return toFraudDetectionResponse(fraudDetection);
            } else {
                fraudDetection = new FraudDetection();
            }

            fraudDetection.setTransactionId(requestDto.getTransactionId());

            if (requestDto.getAmount() > 10000) {
                String reason = "Error: fraud detected";
                fraudDetection.setIsFraudulent(true);
                fraudDetection.setReason(reason);
                fraudDetectionRepository.save(fraudDetection);
            } else {
                fraudDetection.setIsFraudulent(false);
                fraudDetectionRepository.save(fraudDetection);
            }

            return toFraudDetectionResponse(fraudDetection);
        });
    }


} 