package com.devfat.mini_ecommerce.order.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponseDto {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
