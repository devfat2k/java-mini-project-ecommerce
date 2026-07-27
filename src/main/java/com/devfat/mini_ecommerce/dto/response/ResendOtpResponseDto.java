package com.devfat.mini_ecommerce.dto.response;

import lombok.Builder;

@Builder
public record ResendOtpResponseDto(
        String message
) {}