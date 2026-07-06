package com.devfat.mini_ecommerce.dto.request;

import com.devfat.mini_ecommerce.entity.OrderEntity;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequestDto (
        @NotNull OrderEntity.OrderStatus orderStatus
) {}
