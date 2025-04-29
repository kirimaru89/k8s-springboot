package com.example.demo.mapper;


import com.example.demo.models.FraudDetection;
import digitaldocuments.library.v1.FraudDetectionMessages;

public class FraudMapper {

    public static FraudDetectionMessages.FraudDetection toFraudDetectionResponse(FraudDetection fraudDetection) {
        if (fraudDetection.getReason() != null) {
            return FraudDetectionMessages.FraudDetection.newBuilder()
                    .setId(fraudDetection.getId())
                    .setIsFraudulent(fraudDetection.getIsFraudulent())
                    .setReason(fraudDetection.getReason())
                    .build();
        } else {
            return FraudDetectionMessages.FraudDetection.newBuilder()
                    .setId(fraudDetection.getId())
                    .setIsFraudulent(fraudDetection.getIsFraudulent())
                    .build();
        }
    }

}
