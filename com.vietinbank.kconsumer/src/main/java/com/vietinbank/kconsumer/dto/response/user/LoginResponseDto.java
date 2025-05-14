package com.vietinbank.kconsumer.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "LoginResponseDto", description = "Token Response DTO")
public class LoginResponseDto {
    @Schema(
            description = "JWT Token",
            example =
                    "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ2dW9uZ3RyYW4iLCJleHAiOjE3NDE3NTEwODN9.8QIK-yVuDrPtekolqC4RtYypp1UzyVgYXeqaAzmkKtO0bQmjHLnJT3Ws8_rkM_BU")
    String token;
}
