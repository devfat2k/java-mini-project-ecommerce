package com.devfat.mini_ecommerce.auth.dto;











import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponseDto {
    private String accessToken;
}
