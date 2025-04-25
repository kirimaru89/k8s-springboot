package com.vietinbank.paymenthub.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PageableDto", description = "Pageable DTO")
public class PageableDto {
    @Schema(description = "page number", example = "0")
    private Integer page = 0; // Giá trị mặc định là 0

    @Schema(description = "page size", example = "10")
    private Integer size = 10; // Giá trị mặc định là 10

    @Schema(description = "sort by", example = "title,asc")
    private String sort; // VD: "name,asc" hoặc "price,desc"

    public Pageable toPageable() {
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));
            }
            return PageRequest.of(page, size, Sort.by(sortParams[0]));
        }
        return PageRequest.of(page, size);
    }
}
