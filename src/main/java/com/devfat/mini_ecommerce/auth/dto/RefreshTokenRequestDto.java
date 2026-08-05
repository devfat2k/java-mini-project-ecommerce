package com.devfat.mini_ecommerce.auth.dto;











import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequestDto(
        @NotNull
        @NotBlank
        String refreshToken
) {
}
