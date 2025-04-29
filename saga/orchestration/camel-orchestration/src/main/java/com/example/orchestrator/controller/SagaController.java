package com.example.orchestrator.controller;

import com.example.orchestrator.dto.ApiResponseDto;
import com.example.orchestrator.dto.request.transaction.CreateTransactionRequestDto;
import com.example.orchestrator.dto.response.transaction.TransactionResponseDto;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/digital-documents/library/saga")
public class SagaController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/start-transaction")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> startSaga(@RequestBody CreateTransactionRequestDto request) {
        // Trigger the Camel route for saga orchestration
        ApiResponseDto<TransactionResponseDto> res = producerTemplate.requestBody(
                "direct:startTransactionSaga",
                request,
                ApiResponseDto.class
        );
        return ApiResponseDto.done(res);
    }
} 