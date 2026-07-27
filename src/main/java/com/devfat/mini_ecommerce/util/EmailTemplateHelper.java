package com.devfat.mini_ecommerce.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class EmailTemplateHelper {

    private String otpTemplateCache;

    private synchronized String getOtpTemplate() {
        if (otpTemplateCache == null) {
            try {
                Resource resource = new ClassPathResource("templates/email/otp-email-template.html");
                try (InputStream inputStream = resource.getInputStream()) {
                    otpTemplateCache = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.error("Không thể đọc email template otp-email-template.html: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to load email template", e);
            }
        }
        return otpTemplateCache;
    }

    public String buildOtpEmailHtml(
            String userName,
            String purposeText,
            String subjectTitle,
            String otpCode,
            int expireMinutes,
            String actionUrl,
            String actionButtonText
    ) {
        String template = getOtpTemplate();
        return template
                .replace("{{SUBJECT_TITLE}}", subjectTitle != null ? subjectTitle : "Xác Thực OTP")
                .replace("{{USER_NAME}}", userName != null ? userName : "bạn")
                .replace("{{PURPOSE_TEXT}}", purposeText != null ? purposeText : "")
                .replace("{{OTP_CODE}}", otpCode)
                .replace("{{EXPIRE_MINUTES}}", String.valueOf(expireMinutes))
                .replace("{{ACTION_URL}}", actionUrl != null ? actionUrl : "#")
                .replace("{{ACTION_BUTTON_TEXT}}", actionButtonText != null ? actionButtonText : "Mở Trang Xác Thực");
    }
}
