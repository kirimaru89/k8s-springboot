package com.example.demo.dto.response.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAccountResponseDto {
    Long id;
    Long userId;
    Double balance;
}
