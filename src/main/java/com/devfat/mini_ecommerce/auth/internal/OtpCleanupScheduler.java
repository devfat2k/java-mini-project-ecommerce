package com.devfat.mini_ecommerce.auth.internal;











import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpVerificationRepository otpVerificationRepository;

    @Scheduled(cron = "${app.scheduler.otp-cleanup.cron}")   // chạy mỗi ngày lúc 3h sáng — giờ ít traffic
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        otpVerificationRepository.deleteAllExpiredBefore(cutoff);
        log.info("Đã dọn dẹp các OTP hết hạn trước {}", cutoff);
    }
}
