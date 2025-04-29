package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(name = "PaginationResponseDto", description = "Pagination response model")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class PaginationResponseDto {
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private int numberOfElements;
}
