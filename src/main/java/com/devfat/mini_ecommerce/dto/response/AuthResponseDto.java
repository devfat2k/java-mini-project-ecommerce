package com.devfat.mini_ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType =  "Bearer";
    private long expiresIn;
}
