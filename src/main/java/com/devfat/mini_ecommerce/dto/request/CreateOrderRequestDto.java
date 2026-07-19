package com.devfat.mini_ecommerce.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequestDto(
//        @NotNull  Long userId, -- KHÔNG SỬ DỤNG UserId mà sử dụng từ TOKEN lấy từ UserPrincipal
        @NotEmpty @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull  Long productId,
            @Min(1)  Integer quantity
    ) {}
}