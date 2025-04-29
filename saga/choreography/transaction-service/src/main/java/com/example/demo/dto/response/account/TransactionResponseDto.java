package com.example.demo.dto.response.account;

import com.example.demo.enums.TransactionStatus;
import com.example.demo.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponseDto {
    Long id;
    Long accountId;
    Double amount;
    TransactionType type;
    TransactionStatus status;
}
