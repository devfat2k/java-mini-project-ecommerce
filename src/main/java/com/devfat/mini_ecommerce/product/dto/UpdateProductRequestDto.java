package com.devfat.mini_ecommerce.product.dto;











import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateProductRequestDto(
         String name,
         String description,
         @DecimalMin(value = "0", inclusive = false) BigDecimal price,  // đúng kiểu ngay từ đầu
         @Min(1) Integer stock,
         Boolean isActive,
         Long categoryId
)
{}
