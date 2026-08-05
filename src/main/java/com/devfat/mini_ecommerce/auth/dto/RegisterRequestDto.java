package com.devfat.mini_ecommerce.auth.dto;











import jakarta.validation.constraints.*;

public record RegisterRequestDto(
    @NotNull
    @NotBlank(message = "Full name is required!")
    String fullName,

    @NotNull
    @NotBlank(message = "Email is required!")
    @Email
    @Size(min = 5, max = 255, message = "Email is valid patent!")
    String email,

    @NotNull
    @NotBlank(message = "Phone number is required!")
    @Pattern(
            regexp = "^[0-9]{10,11}$",
            message = "Phone number must be 10-11 digits!"
    )
    String phoneNumber,

    @NotNull
    @NotBlank(message = "Password is required!")
    @Size(min = 8, max = 100, message = "Password is min 8 and max 100 character!")
    String password
)
{}
