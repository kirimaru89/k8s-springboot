package com.example.demo.dto.response.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListAccountsResponseDto {
    List<GetAccountResponseDto> accounts;
}
