package com.example.demo.controllers;

import com.example.demo.dto.request.fraud.CheckFraudTransactionRequestDto;
import com.example.demo.dto.response.fraud.CheckFraudTransactionResponseDto;
import com.example.demo.services.FraudService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.response.*;

import io.swagger.v3.oas.annotations.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/digital-documents/library/fraud-detection")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FraudController {
    FraudService fraudService;

    // saga
    @Operation(summary = "Check Fraud Detection Transaction", description = "Check Fraud Detection Transaction API")
    @PostMapping
    public ResponseEntity<ApiResponseDto<CheckFraudTransactionResponseDto>> completeUpdateBalance(@Valid @RequestBody CheckFraudTransactionRequestDto requestDto) {
        CheckFraudTransactionResponseDto responseDto = fraudService.fraudDetection(requestDto);
        log.info("Check Fraud Detection Transaction: {}", requestDto.getTransactionId() );
        return ApiResponseDto.success(responseDto);
    }
}
