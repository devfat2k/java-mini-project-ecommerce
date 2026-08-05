package com.devfat.mini_ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequestDto(
        @NotBlank(message = "Full name is required!")
        String fullName,

        @NotBlank(message = "Phone number is required!")
        @Pattern(
                regexp = "^[0-9]{10,11}$",
                message = "Phone number must be 10-11 digits!"
        )
        String phoneNumber
) {}
