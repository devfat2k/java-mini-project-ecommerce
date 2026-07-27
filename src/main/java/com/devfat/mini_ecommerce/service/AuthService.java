package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.*;
import com.devfat.mini_ecommerce.dto.response.*;


public interface AuthService {
    UserResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
    void logout(RefreshTokenRequestDto refreshTokenRequestDto);
    void forgotPassword(ForgotPasswordRequestDto dto);
    VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto dto);
    ResendOtpResponseDto resendOtp(ResendOtpRequestDto dto);
}
