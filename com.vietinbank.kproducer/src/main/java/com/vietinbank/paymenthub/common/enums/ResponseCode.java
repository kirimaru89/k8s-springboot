package com.vietinbank.paymenthub.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    // Success (2xx)
    SUCCESS(HttpStatus.OK, "Success"),
    CREATED(HttpStatus.CREATED, "Created"),
    NO_CONTENT(HttpStatus.NO_CONTENT, null),

    // Validation Errors (4xx)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),

    // Authentication & Authorization Errors
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),

    // Resource Errors
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),

    // Business Logic Errors
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),

    // System Errors (5xx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable"),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Gateway timeout"),
    ;

    private final HttpStatusCode code;
    private final String message;

    public String getCodeString() {
        return String.valueOf(code.value());
    }
}
