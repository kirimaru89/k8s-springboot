package com.vietinbank.paymenthub.dto.request.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(name = "BookFilterDto", description = "Book Filter DTO")
public class BookFilterDto {
    @Schema(description = "title", example = "Habit")
    private String title;

    @Schema(description = "author", example = "Odin")
    private String author;

    @Schema(description = "userId") //, example = "Done,Pending,Failed")
    private List<Long> userId;
}
