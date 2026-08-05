package com.devfat.mini_ecommerce.auth;

import com.devfat.mini_ecommerce.auth.dto.AuthResponseDto;
import com.devfat.mini_ecommerce.auth.dto.ForgotPasswordRequestDto;
import com.devfat.mini_ecommerce.auth.dto.LoginRequestDto;
import com.devfat.mini_ecommerce.auth.dto.RefreshTokenRequestDto;
import com.devfat.mini_ecommerce.auth.dto.RefreshTokenResponseDto;
import com.devfat.mini_ecommerce.auth.dto.RegisterRequestDto;
import com.devfat.mini_ecommerce.auth.dto.ResendOtpRequestDto;
import com.devfat.mini_ecommerce.auth.dto.ResendOtpResponseDto;
import com.devfat.mini_ecommerce.auth.dto.VerifyOtpRequestDto;
import com.devfat.mini_ecommerce.auth.dto.VerifyOtpResponseDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;



public interface AuthService {
    UserResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
    void logout(RefreshTokenRequestDto refreshTokenRequestDto);
    void forgotPassword(ForgotPasswordRequestDto dto);
    VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto dto);
    ResendOtpResponseDto resendOtp(ResendOtpRequestDto dto);
}
