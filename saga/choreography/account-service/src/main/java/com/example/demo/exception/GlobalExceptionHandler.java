package com.example.demo.exception;

import java.net.SocketTimeoutException;
import java.util.stream.Collectors;

import com.example.demo.dto.response.ApiResponseDto;
import com.example.demo.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
@Slf4j
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
    public ResponseEntity<ApiResponseDto<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.error("HttpRequestMethodNotSupportedException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.METHOD_NOT_ALLOWED, exception.getMessage());
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSocketTimeoutException(SocketTimeoutException exception) {
        log.error("SocketTimeoutException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.GATEWAY_TIMEOUT, exception.getMessage());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceAccessException(ResourceAccessException exception) {
        log.error("ResourceAccessException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRuntimeException(RuntimeException exception) {
        log.error("RuntimeException: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage(), exception);
        return ApiResponseDto.error(ResponseCode.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

}
