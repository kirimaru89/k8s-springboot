package com.vietinbank.kproducer.dto.request.book;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BookCreationDto", description = "Book Creation DTO")
public class BookCreationDto {

    @Schema(description = "Title", example = "7 Habits")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Author", example = "Salmon Odin")
    @NotBlank(message = "Author is required")
    private String author;
}
