package com.devfat.mini_ecommerce.category.dto;

import lombok.*;

@NoArgsConstructor
@Builder
@Getter
@Setter
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
    private Long productCount; // đổi Integer -> Long, vì COUNT() trả về Long

    public CategoryResponseDto(Long id, String categoryName, Long productCount) {
        this.id = id;
        this.categoryName = categoryName;
        this.productCount = productCount;
    }
}