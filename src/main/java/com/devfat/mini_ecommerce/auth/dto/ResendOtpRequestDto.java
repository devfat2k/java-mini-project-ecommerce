package com.devfat.mini_ecommerce.auth.dto;

import com.devfat.mini_ecommerce.auth.OtpPurpose;










import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResendOtpRequestDto(
        @NotBlank @Email String email,
        @NotNull OtpPurpose purpose
) {
}
