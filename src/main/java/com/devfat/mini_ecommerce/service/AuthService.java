package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.LoginRequestDto;
import com.devfat.mini_ecommerce.dto.request.RefreshTokenRequestDto;
import com.devfat.mini_ecommerce.dto.request.RegisterRequestDto;
import com.devfat.mini_ecommerce.dto.response.AuthResponseDto;
import com.devfat.mini_ecommerce.dto.response.RefreshTokenResponseDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;


public interface AuthService {
    UserResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
    void logout(RefreshTokenRequestDto refreshTokenRequestDto);
}
