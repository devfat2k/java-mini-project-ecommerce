package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.entity.OtpVerificationEntity;
import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.enums.OtpPurpose;

public interface OtpService {
    String generateAndSendOtp(UserEntity user, OtpPurpose purpose);
    OtpVerificationEntity verifyOtp(Long userId, OtpPurpose purpose, String rawOtpInput);
    boolean resetOtp(Long userId, OtpPurpose purpose);
}
