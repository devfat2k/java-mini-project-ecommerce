package com.devfat.mini_ecommerce.user.address.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAddressRequestDto(
        @NotNull String  recipientName,
        @NotNull String  phone,
        @NotNull String  province,
        @NotNull String  district,
        @NotNull String  ward,
        @NotNull String  addressDetail,
        @NotNull boolean defaultAddress,
        @NotNull String  tag
) {
}
