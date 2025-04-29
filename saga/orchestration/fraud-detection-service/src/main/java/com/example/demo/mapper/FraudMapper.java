package com.example.demo.mapper;

import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.models.FraudDetection;

public class FraudMapper {
   public static CheckFraudTransactionResponseDto toResponseDto(FraudDetection fraudDetection) {
       return CheckFraudTransactionResponseDto.builder()
               .isFraudulent(fraudDetection.getIsFraudulent())
               .reason(fraudDetection.getReason())
               .build();
   }
}
