package com.vietinbank.paymenthub.exception;

import java.net.SocketTimeoutException;
import java.util.stream.Collectors;

import com.vietinbank.paymenthub.dto.response.ApiResponseDto;
import com.vietinbank.paymenthub.common.ResponseCode;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        // get errors from BindingResult
        String msg = exception.getBindingResult().getFieldErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));

        log.error("MethodArgumentNotValidException: {}", msg, exception);

        return ApiResponseDto.error(ResponseCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);
        return ApiResponseDto.error(ResponseCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        log.error("AccessDeniedException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AuthenticationException exception) {
        log.error("AuthenticationException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.UNAUTHORIZED);
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSocketTimeoutException(SocketTimeoutException exception) {
        log.error("SocketTimeoutException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.GATEWAY_TIMEOUT);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceAccessException(ResourceAccessException exception) {
        log.error("ResourceAccessException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRuntimeException(RuntimeException exception) {
        log.error("RuntimeException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
