package com.devfat.mini_ecommerce.category.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
    private Long productCount;
    private String imageUrl;
}