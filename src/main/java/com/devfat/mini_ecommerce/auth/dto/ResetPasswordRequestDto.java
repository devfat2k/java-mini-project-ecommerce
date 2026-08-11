package com.devfat.mini_ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotNull
        @NotBlank(message = "Token is required!")
        String actionToken,
        @NotNull
        @NotBlank(message = "Password is required!")
        @Size(min = 8, max = 100, message = "Password is min 8 and max 100 character!")
        String newPassword
) {
}
