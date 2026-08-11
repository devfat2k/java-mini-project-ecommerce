package com.devfat.mini_ecommerce.auth;

import com.devfat.mini_ecommerce.auth.internal.OtpVerificationEntity;
import com.devfat.mini_ecommerce.user.internal.UserEntity;

public interface OtpService {
    String generateAndSendOtp(UserEntity user, OtpPurpose purpose);
    OtpVerificationEntity verifyOtp(Long userId, OtpPurpose purpose, String rawOtpInput);
    boolean resetOtp(Long userId, OtpPurpose purpose);
}
