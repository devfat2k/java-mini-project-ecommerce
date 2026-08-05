package com.devfat.mini_ecommerce.category.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
}
