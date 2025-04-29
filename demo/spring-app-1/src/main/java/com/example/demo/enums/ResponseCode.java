package com.example.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    // Success codes (2xx)
    SUCCESS("200", "Success", "Thành công"),
    CREATED("201", "Created", "Đã tạo thành công"),

    // Client Error codes (4xx)
    BAD_REQUEST("400", "Bad Request", "Yêu cầu không hợp lệ"),
    UNAUTHORIZED("401", "Unauthorized", "Chưa xác thực"),
    FORBIDDEN("403", "Forbidden", "Không có quyền truy cập"),
    NOT_FOUND("404", "Not Found", "Không tìm thấy"),
    CONFLICT("409", "Conflict", "Xung đột dữ liệu"),
    TOO_MANY_REQUESTS("429", "Too Many Requests", "Đã vượt quá số lượng yêu cầu cho phép"),

    // Server Error codes (5xx)
    INTERNAL_SERVER_ERROR("500", "Internal Server Error", "Lỗi hệ thống"),
    SERVICE_UNAVAILABLE("503", "Service Unavailable", "Dịch vụ không khả dụng"),
    GATEWAY_TIMEOUT("504", "Gateway Timeout", "Cổng kết nối hết thời gian chờ"),

    // Custom error codes
    CIRCUIT_BREAKER_OPEN("503", "Service Unavailable", "Hệ thống tạm thời không khả dụng"),
    BULKHEAD_FULL("503", "System Overloaded", "Hệ thống đang quá tải"),
    RATE_LIMIT_EXCEEDED("429", "Rate Limit Exceeded", "Đã vượt quá giới hạn số lượng yêu cầu"),
    TIMEOUT_ERROR("504", "Request Timeout", "Yêu cầu đã hết thời gian chờ");

    private final String code;
    private final String message;
    private final String viMessage;
} 