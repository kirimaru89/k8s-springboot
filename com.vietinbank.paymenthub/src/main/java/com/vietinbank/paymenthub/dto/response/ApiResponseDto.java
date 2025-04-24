package com.vietinbank.paymenthub.dto.response;

import com.vietinbank.paymenthub.common.ResponseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiResponseDto", description = "Generic response wrapper")
public class ApiResponseDto<T> {
    @Schema(description = "Response Status")
    @Builder.Default
    private int code = HttpStatus.OK.value();

    @Schema(description = "Response Message")
    private String message;

    @Schema(description = "Response Data")
    private T data;

    public ApiResponseDto(ResponseCode code) {
        this.code = code.getCode().value();
        this.message = code.getMessage();
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> success(T data) {
        ApiResponseDto<T> response = ApiResponseDto.<T>builder()
                .code(ResponseCode.SUCCESS.getCode().value())
                .message(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> success(ResponseCode code, T data) {
        ApiResponseDto<T> response = ApiResponseDto.<T>builder()
                .code(code.getCode().value())
                .message(code.getMessage())
                .data(data)
                .build();
        return ResponseEntity.status(code.getCode()).body(response);
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> error(ResponseCode responseCode) {
        ApiResponseDto<T> response = ApiResponseDto.<T>builder()
                .code(responseCode.getCode().value())
                .message(responseCode.getMessage())
                .build();
        return new ResponseEntity<>(response, responseCode.getCode());
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> error(ResponseCode responseCode, String additionalMessage) {
        ApiResponseDto<T> response = ApiResponseDto.<T>builder()
                .code(responseCode.getCode().value())
                .message(responseCode.getMessage() + ": " + additionalMessage)
                .build();
        return new ResponseEntity<>(response, responseCode.getCode());
    }
}
