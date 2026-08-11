package com.devfat.mini_ecommerce.order.dto;

import com.devfat.mini_ecommerce.payment.PaymentMethod;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;









import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequestDto(
        @NotNull PaymentMethod paymentMethod,
        String note,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull Long productId,
            @Min(1) Integer quantity
    ) {}
}
