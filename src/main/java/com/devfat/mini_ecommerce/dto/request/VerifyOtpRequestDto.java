package com.devfat.mini_ecommerce.dto.request;

import com.devfat.mini_ecommerce.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}") String otpCode,
        @NotNull OtpPurpose purpose
) {}