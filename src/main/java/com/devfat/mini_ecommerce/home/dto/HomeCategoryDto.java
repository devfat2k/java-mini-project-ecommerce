package com.devfat.mini_ecommerce.home.dto;

public record HomeCategoryDto(
        Long id,
        String name,
        String description,
        String slug,
        String imageUrl,
        String badge,
        String badgeType,    // "hot" | "number" | "fresh" | "dry"
        String iconName,     // Lucide icon name
        long productCount,
        String displayStyle, // "main" | "card" | "icon"
        Integer sortOrder,
        boolean isActive
) {}
