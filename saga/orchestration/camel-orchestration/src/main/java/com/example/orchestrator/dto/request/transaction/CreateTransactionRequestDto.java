package com.example.orchestrator.dto.request.transaction;

import com.example.orchestrator.enums.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTransactionRequestDto {
    Long accountId;
    Double amount;
    TransactionType type;
}
