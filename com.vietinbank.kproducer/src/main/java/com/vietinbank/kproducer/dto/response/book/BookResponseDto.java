package com.vietinbank.kproducer.dto.response.book;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "BookResponseDto", description = "Book Response DTO")
public class BookResponseDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID", example = "1")
    private long id;

    @Schema(description = "Title", example = "7 Habits")
    private String title;

    @Schema(description = "Author", example = "Salmon Odin")
    private String author;
}
