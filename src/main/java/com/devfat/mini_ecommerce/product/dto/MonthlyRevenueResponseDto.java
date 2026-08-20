package com.devfat.mini_ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonthlyRevenueResponseDto(
        LocalDateTime month,
        BigDecimal revenue
) {}
