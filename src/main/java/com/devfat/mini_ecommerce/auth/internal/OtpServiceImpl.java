package com.devfat.mini_ecommerce.auth.internal;

import com.devfat.mini_ecommerce.auth.OtpPurpose;
import com.devfat.mini_ecommerce.auth.OtpService;
import com.devfat.mini_ecommerce.auth.exception.OtpAttemptsExceededException;
import com.devfat.mini_ecommerce.auth.exception.OtpExpiredException;
import com.devfat.mini_ecommerce.auth.exception.OtpInvalidException;
import com.devfat.mini_ecommerce.auth.exception.OtpNotFoundException;
import com.devfat.mini_ecommerce.notification.EmailService;
import com.devfat.mini_ecommerce.notification.dto.EmailRequestDto;
import com.devfat.mini_ecommerce.notification.internal.EmailTemplateHelper;
import com.devfat.mini_ecommerce.user.internal.UserEntity;










import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final EmailService emailService;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailTemplateHelper emailTemplateHelper;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private String hashOtp(String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");   // tạo MỚI mỗi lần gọi — thread-safe
            byte[] hashBytes = digest.digest(rawOtp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);   // Java 17+ có sẵn HexFormat, không cần thư viện ngoài
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e); // gần như không bao giờ xảy ra
        }
    }

    private String resolveSubject(OtpPurpose purpose) {
        return switch (purpose) {
            case REGISTER_VERIFICATION -> "Xác Thực Địa Chỉ Email - Mini Seafood Shop";
            case RESET_PASSWORD -> "Khôi Phục Mật Khẩu - Mini Seafood Shop";
            case CHANGE_PASSWORD_CONFIRMATION -> "Xác Nhận Đổi Mật Khẩu - Mini Seafood Shop";
        };
    }

    private String resolvePurposeText(OtpPurpose purpose) {
        return switch (purpose) {
            case REGISTER_VERIFICATION -> "Cảm ơn bạn đã đăng ký tài khoản tại Mini Seafood Shop. Để hoàn tất đăng ký, vui lòng sử dụng mã xác thực OTP bên dưới.";
            case RESET_PASSWORD -> "Hệ thống đã nhận được yêu cầu khôi phục mật khẩu tài khoản của bạn. Vui lòng sử dụng mã OTP bên dưới để đặt lại mật khẩu.";
            case CHANGE_PASSWORD_CONFIRMATION -> "Bạn đang thực hiện thay đổi mật khẩu tài khoản. Vui lòng sử dụng mã OTP bên dưới để xác nhận thay đổi.";
        };
    }

    @Override
    @Transactional
    public String generateAndSendOtp(UserEntity user, OtpPurpose purpose) {
        // BƯỚC 1 — Sinh 6 chữ số bằng SecureRandom
        SecureRandom secureRandom = new SecureRandom();
        int otpNumber = secureRandom.nextInt(900_000) + 100_000;
        String rawOtp = String.valueOf(otpNumber);
        // BƯỚC 2 — Hash bằng SHA-256, KHÔNG lưu rawOtp vào đâu cả ngoài biến local này
        String otpHash = hashOtp(rawOtp);
        // BƯỚC 3 — Build entity, lưu DB
        OtpVerificationEntity otpEntity = OtpVerificationEntity.builder()
                .user(user)
                .otpHash(otpHash)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .consumed(false)
                .build();
        otpVerificationRepository.save(otpEntity);
        // BƯỚC 4 — Gửi email HTML (tái dùng EmailService + EmailTemplateHelper)
        String subject = resolveSubject(purpose);
        String purposeText = resolvePurposeText(purpose);

        String htmlContent = emailTemplateHelper.buildOtpEmailHtml(
                user.getFullName(),
                purposeText,
                subject,
                rawOtp,
                5,
                frontendUrl,
                "Mở Trang Xác Thực (Swagger UI)"
        );

        log.info("Gửi email OTP ({}) tới: {}", purpose, user.getEmail());

        emailService.sendHtmlEmail(new EmailRequestDto(user.getEmail(), subject, htmlContent));
        // BƯỚC 5 — Trả rawOtp về
        return rawOtp;
    }

    @Override
    @Transactional
    public OtpVerificationEntity verifyOtp(Long userId, OtpPurpose purpose, String rawOtpInput) {
        OtpVerificationEntity verificationEntity = otpVerificationRepository
                .findFirstByUser_IdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(userId, purpose)
                .orElseThrow(() -> new OtpNotFoundException("Otp not found!"));

        if(LocalDateTime.now().isAfter(verificationEntity.getExpiresAt())) {
            throw new OtpExpiredException("Otp expired!");
        }

        if (verificationEntity.getAttempts() >= 5) {
            throw new OtpAttemptsExceededException("Otp attempts exceeded!");
        }
        String inputHash = hashOtp(rawOtpInput);
        if (!inputHash.equals(verificationEntity.getOtpHash())) {
            verificationEntity.setAttempts(verificationEntity.getAttempts() + 1);
            otpVerificationRepository.save(verificationEntity);
            throw new OtpInvalidException("Otp invalid!");
        }

        verificationEntity.setConsumed(true);
        otpVerificationRepository.save(verificationEntity);
        return verificationEntity;
    }

    @Override
    public boolean resetOtp(Long userId, OtpPurpose purpose) {
        return otpVerificationRepository
                .findFirstByUser_IdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(userId, purpose)
                .map(latest -> LocalDateTime.now().isAfter(latest.getCreatedAt().plusSeconds(60)))
                .orElse(true);
    }
}
