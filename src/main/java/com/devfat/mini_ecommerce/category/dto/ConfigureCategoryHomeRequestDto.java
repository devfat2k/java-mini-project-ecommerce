package com.devfat.mini_ecommerce.category.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
public record ConfigureCategoryHomeRequestDto(
        @Size(max = 100, message = "Badge max length is 100 characters")
        String badge,
        @Size(max = 20, message = "Badge type max length is 20 characters")
        String badgeType,          // "hot" | "number" | "fresh" | "dry"
        @Size(max = 50, message = "Icon name max length is 50 characters")
        String iconName,           // Lucide icon name (vd: "fish")
        @Size(max = 10, message = "Display style max length is 10 characters")
        String homeDisplayStyle,   // "main" | "card" | "icon"
        @Min(value = 0, message = "Sort order must be greater than or equal to 0")
        Integer homeSortOrder,
        Boolean homeIsActive
) {
}
