package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.RegisterRequestDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;

public interface AuthService {
    UserResponseDto register(RegisterRequestDto registerRequestDto);
}
