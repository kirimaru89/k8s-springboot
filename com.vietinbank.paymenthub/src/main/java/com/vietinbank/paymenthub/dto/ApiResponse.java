package com.vietinbank.paymenthub.dto;

import com.vietinbank.paymenthub.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.vietinbank.paymenthub.utils.LanguageUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String code;
    private String message;
    private long timestamp;
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        String language = LanguageUtils.getCurrentLanguage();
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(getLocalizedMessage(ResponseCode.SUCCESS, language))
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(ResponseCode responseCode) {
        String language = LanguageUtils.getCurrentLanguage();
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(getLocalizedMessage(responseCode, language))
                .timestamp(System.currentTimeMillis())
                .build();
        return new ResponseEntity<>(response, getHttpStatus(responseCode));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(ResponseCode responseCode, String additionalMessage) {
        String language = LanguageUtils.getCurrentLanguage();
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(getLocalizedMessage(responseCode, language) + ": " + additionalMessage)
                .timestamp(System.currentTimeMillis())
                .build();
        return new ResponseEntity<>(response, getHttpStatus(responseCode));
    }

    private static HttpStatus getHttpStatus(ResponseCode responseCode) {
        switch (responseCode) {
            case BAD_REQUEST:
                return HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            case NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case INTERNAL_SERVER_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case CIRCUIT_BREAKER_OPEN:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case BULKHEAD_FULL:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case RATE_LIMIT_EXCEEDED:
                return HttpStatus.TOO_MANY_REQUESTS;
            case TIMEOUT_ERROR:
                return HttpStatus.GATEWAY_TIMEOUT;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private static String getLocalizedMessage(ResponseCode responseCode, String language) {
        if ("vi".equalsIgnoreCase(language)) {
            return responseCode.getViMessage();
        }
        return responseCode.getMessage();
    }
} 