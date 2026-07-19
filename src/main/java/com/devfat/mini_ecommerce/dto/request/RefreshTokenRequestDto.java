package com.devfat.mini_ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequestDto(
        @NotNull
        @NotBlank
        String refreshToken
) {
}
