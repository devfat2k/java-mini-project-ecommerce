package com.devfat.mini_ecommerce.order.dto;

import com.devfat.mini_ecommerce.order.OrderStatus;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDto {
    private long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> orderItems;
    private String shippingAddress;
    private String paymentMethod;
    private String shippingAddressSnapShot;
}
