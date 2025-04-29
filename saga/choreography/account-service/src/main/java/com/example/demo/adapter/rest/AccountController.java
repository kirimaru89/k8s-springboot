package com.example.demo.adapter.rest;

import com.example.demo.dto.message.fraud.FraudTransactionDetectedMessageDto;
import com.example.demo.dto.message.fraud.FraudTransactionVerifiedMessageDto;
import com.example.demo.dto.message.TransactionInitiatedMessageDto;
import com.example.demo.dto.request.account.CreateAccountRequestDto;
import com.example.demo.dto.request.account.ListAccountsRequestDto;
import com.example.demo.dto.response.account.CreateAccountResponseDto;
import com.example.demo.dto.response.account.GetAccountResponseDto;
import com.example.demo.dto.response.account.ListAccountsResponseDto;
import com.example.demo.services.AccountService;
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
@RequestMapping("/api/v1/digital-documents/library/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountController {
    AccountService accountService;

    // restful api
    @Operation(summary = "Get Accounts List", description = "Get Accounts List API")
    @GetMapping
    public ResponseEntity<ApiResponseDto<ListAccountsResponseDto>> list(
            @ModelAttribute ListAccountsRequestDto requestDto
            ) {
        ListAccountsResponseDto res = accountService.getAccountsByUserId(requestDto.getUserId());
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "Create Account", description = "Create Account API")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDto<CreateAccountResponseDto>> create(@Valid @RequestBody CreateAccountRequestDto requestDto) {
        CreateAccountResponseDto res = accountService.createAccount(requestDto);
        log.info("create account: {}, {}, {}", res.getId(), res.getUserId(), res.getBalance() );
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "Get the account", description = "Get the account API")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<GetAccountResponseDto>> get(
            @Parameter(description = "ID of the account to be fetched") @PathVariable Long id) {
        GetAccountResponseDto res = accountService.getAccountById(id);
        log.info("get account: {}, {}, {}", res.getId(), res.getUserId(), res.getBalance() );
        return ApiResponseDto.success(res);
    }
}
