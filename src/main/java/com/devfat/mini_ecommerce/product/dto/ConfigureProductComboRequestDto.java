package com.devfat.mini_ecommerce.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ConfigureProductComboRequestDto(
        @Size(max = 50, message = "Combo category max length is 50 characters")
        String comboCategory, // "lunch" | "party" | "family"
        @Size(max = 20, message = "Combo theme max length is 20 characters")
        String comboTheme,    // "light" | "dark"
        @Size(max = 50, message = "Combo tag max length is 50 characters")
        String comboTag,
        @Size(max = 100, message = "Combo CTA text max length is 100 characters")
        String comboCtaText,
        @Size(max = 255, message = "Combo href max length is 255 characters")
        String comboHref,
        Boolean isBreakout,
        @Min(value = 0, message = "Sort order must be greater than or equal to 0")
        Integer comboSortOrder
) {}