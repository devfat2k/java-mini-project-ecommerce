package com.devfat.mini_ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(

        @NotNull
        @NotBlank(message = "Old Password is required!")
        @Size(min = 8, max = 100, message = "Password is min 8 and max 100 character!")
        String oldPassword,

        @NotNull
        @NotBlank(message = "Old Password is required!")
        @Size(min = 8, max = 100, message = "Password is min 8 and max 100 character!")
        String newPassword
){}
