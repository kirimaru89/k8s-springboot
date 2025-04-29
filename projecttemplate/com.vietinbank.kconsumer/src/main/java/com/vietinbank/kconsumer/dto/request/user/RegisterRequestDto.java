package com.vietinbank.kproducer.dto.request.user;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "RegisterRequestDto", description = "Register Request DTO")
public class RegisterRequestDto {
    @Schema(description = "Username", example = "my-username")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password", example = "my-password")
    @NotBlank(message = "Password is required")
    private String password;
}
