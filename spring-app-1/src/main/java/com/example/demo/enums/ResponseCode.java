package com.example.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    // Success codes
    SUCCESS("200", "Thành công"),

    // Client Error codes (4xx)
    BAD_REQUEST("400", "Dữ liệu không hợp lệ"),
    UNAUTHORIZED("401", "Chưa xác thực"),
    FORBIDDEN("403", "Không có quyền truy cập"),
    NOT_FOUND("404", "Không tìm thấy tài nguyên"),
    METHOD_NOT_ALLOWED("405", "Phương thức không được hỗ trợ"),
    REQUEST_TIMEOUT("408", "Yêu cầu đã hết thời gian chờ"),
    TOO_MANY_REQUESTS("429", "Đã vượt quá số lượng yêu cầu cho phép"),

    // Server Error codes (5xx)
    INTERNAL_SERVER_ERROR("500", "Lỗi hệ thống"),
    SERVICE_UNAVAILABLE("503", "Dịch vụ tạm thời không khả dụng"),
    GATEWAY_TIMEOUT("504", "Gateway timeout"),

    // Custom error codes
    CIRCUIT_BREAKER_OPEN("503", "Hệ thống tạm thời không khả dụng"),
    BULKHEAD_FULL("503", "Hệ thống đang quá tải"),
    RATE_LIMIT_EXCEEDED("429", "Đã vượt quá giới hạn số lượng yêu cầu"),
    TIMEOUT_ERROR("504", "Yêu cầu đã hết thời gian chờ");

    private final String code;
    private final String message;
} 