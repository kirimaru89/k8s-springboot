package com.example.demo.dto.response.fraud;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckFraudTransactionResponseDto {
    Boolean isFraudulent;
    String reason;
}
