package com.example.orchestrator.dto.response.transaction;

import com.example.orchestrator.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDto {
    Long id;
    Long accountId;
    Double amount;
    TransactionType type;
}
