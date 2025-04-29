package com.example.demo.controllers;

import com.example.demo.dto.request.transaction.CreateTransactionRequestDto;
import com.example.demo.dto.request.transaction.ListTransactionsRequestDto;
import com.example.demo.dto.request.transaction.UpdateTransactionStatusDto;
import com.example.demo.dto.response.account.TransactionResponseDto;
import com.example.demo.dto.response.account.ListTransactionsResponseDto;
import com.example.demo.enums.ResponseCode;
import com.example.demo.services.TransactionService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.response.*;

import io.swagger.v3.oas.annotations.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/digital-documents/library/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionController {
    TransactionService transactionService;

    // restful api
    @Operation(summary = "Get Transactions List", description = "Get Transactions List API")
    @GetMapping
    public ResponseEntity<ApiResponseDto<ListTransactionsResponseDto>> list(
            @ModelAttribute ListTransactionsRequestDto requestDto
            ) {
        ListTransactionsResponseDto res = transactionService.list(requestDto.getAccountId());
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "Get the transaction", description = "Get the transaction API")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> get(
            @Parameter(description = "ID of the transaction to be fetched") @PathVariable Long id) {
        TransactionResponseDto res = transactionService.get(id);
        log.info("get transaction: {}, {}, {}", res.getId(), res.getType(), res.getAmount() );
        return ApiResponseDto.success(res);
    }

    // saga
    @Operation(summary = "Create Transaction", description = "Create Transaction API")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> create(@Valid @RequestBody CreateTransactionRequestDto requestDto) {
        TransactionResponseDto res = transactionService.create(requestDto);
        log.info("create transaction: {}, {}, {}", res.getId(), res.getType(), res.getAmount() );

        return ApiResponseDto.success(ResponseCode.CREATED, res);
//        return ApiResponseDto.error(ResponseCode.BAD_REQUEST, "add more");
    }

    @Operation(summary = "Rollback Update Status", description = "Rollback Update status API")
    @PostMapping("/rollback")
    public ResponseEntity<ApiResponseDto<Void>> rollbackUpdateStatus(@Valid @RequestBody UpdateTransactionStatusDto requestDto) {
        transactionService.rollbackUpdateStatus(requestDto.getTransactionId());
        log.info("rollback update status: {}", requestDto.getTransactionId() );
        return ApiResponseDto.success(null);
    }

    @Operation(summary = "Complete Update Status", description = "Complete Update status API")
    @PostMapping("/complete")
    public ResponseEntity<ApiResponseDto<Void>> completeUpdateStatus(@Valid @RequestBody UpdateTransactionStatusDto requestDto) {
        transactionService.completeUpdateStatus(requestDto.getTransactionId());
        log.info("complete update status: {}", requestDto.getTransactionId() );
        return ApiResponseDto.success(null);
    }
}
