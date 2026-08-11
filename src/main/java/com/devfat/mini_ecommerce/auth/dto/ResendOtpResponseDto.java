package com.devfat.mini_ecommerce.auth.dto;











import lombok.Builder;

@Builder
public record ResendOtpResponseDto(
        String message
) {}
