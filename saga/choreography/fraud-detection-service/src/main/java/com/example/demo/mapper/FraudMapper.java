package com.example.demo.mapper;

import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.models.FraudDetection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FraudMapper {
   public static CheckFraudTransactionResponseDto toResponseDto(FraudDetection fraudDetection) {
       return CheckFraudTransactionResponseDto.builder()
               .isFraudulent(fraudDetection.getIsFraudulent())
               .reason(fraudDetection.getReason())
               .build();
   }

    // message
    public static <T> String toMessage(T t) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize account response", e);
        }
    }


    public static <T> T parseMessage(String jsonStrMessage, TypeReference<T> typeReference) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonStrMessage, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse message", e);
        }
    }

}
