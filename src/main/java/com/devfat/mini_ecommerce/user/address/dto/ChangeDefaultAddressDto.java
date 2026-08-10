package com.devfat.mini_ecommerce.user.address.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeDefaultAddressDto(
        @NotNull boolean defaultAddress
) {}
