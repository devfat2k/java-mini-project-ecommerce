package com.devfat.mini_ecommerce.product.dto;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;










import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private boolean active;
    private CategoryResponseDto category;
}
