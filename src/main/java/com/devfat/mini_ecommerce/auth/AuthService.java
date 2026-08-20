package com.devfat.mini_ecommerce.auth;

import com.devfat.mini_ecommerce.auth.dto.*;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;


public interface AuthService {
    UserResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
    void logout(RefreshTokenRequestDto refreshTokenRequestDto);
    void forgotPassword(ForgotPasswordRequestDto dto);
    VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto dto);
    ResendOtpResponseDto resendOtp(ResendOtpRequestDto dto);
    void resetPassword(ResetPasswordRequestDto dto);
}
