package com.devfat.mini_ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotNull
        @NotBlank(message = "Email is required!")
        @Size(min = 5, max = 255, message = "Email is valid patent!")
        @Email
        String email,

        @NotBlank(message = "Password is required!")
        @Size(min = 8, max = 100, message = "Password is min 8 and max 100 character!")
        String password
)
{}
