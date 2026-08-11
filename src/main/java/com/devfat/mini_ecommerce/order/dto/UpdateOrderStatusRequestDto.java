package com.devfat.mini_ecommerce.order.dto;

import com.devfat.mini_ecommerce.order.OrderStatus;










import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequestDto (
        @NotNull OrderStatus orderStatus
) {}
