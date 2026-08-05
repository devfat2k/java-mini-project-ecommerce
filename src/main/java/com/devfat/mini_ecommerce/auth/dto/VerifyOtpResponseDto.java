package com.devfat.mini_ecommerce.auth.dto;












import lombok.Builder;

@Builder
public record VerifyOtpResponseDto(
        String accessToken,     // chỉ có giá trị khi purpose = REGISTER_VERIFICATION
        String refreshToken,    // chỉ có giá trị khi purpose = REGISTER_VERIFICATION
        String actionToken      // chỉ có giá trị khi purpose = RESET_PASSWORD hoặc CHANGE_PASSWORD_CONFIRMATION
) {}
