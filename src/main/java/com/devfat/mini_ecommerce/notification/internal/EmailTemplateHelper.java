package com.devfat.mini_ecommerce.notification.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class EmailTemplateHelper {

    // ─── OTP Template ───────────────────────────────────────────────────────────
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

    // ─── Order / Payment Email Template ─────────────────────────────────────────
    private String orderEmailTemplateCache;

    private synchronized String getOrderEmailTemplate() {
        if (orderEmailTemplateCache == null) {
            try {
                Resource resource = new ClassPathResource("templates/email/order-email-template.html");
                try (InputStream inputStream = resource.getInputStream()) {
                    orderEmailTemplateCache = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.error("Không thể đọc email template order-email-template.html: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to load order email template", e);
            }
        }
        return orderEmailTemplateCache;
    }

    /**
     * Build HTML email dùng chung cho đặt hàng thành công và thanh toán thành công.
     * Phân biệt nội dung, màu sắc, icon qua {@link OrderEmailType}.
     *
     * @param type    ORDER_PLACED hoặc PAYMENT_SUCCESS
     * @param orderId ID đơn hàng trong hệ thống
     * @param actionUrl URL FE để xem đơn hàng (có thể null — fallback về "#")
     */
    public String buildOrderEmailHtml(OrderEmailType type, Long orderId, String actionUrl) {
        String template = getOrderEmailTemplate();

        String icon, headerGradient, subjectTitle, statusText, statusBadge, badgeBg, badgeColor, actionText;

        if (type == OrderEmailType.ORDER_PLACED) {
            icon            = "&#128230;";   // 📦
            headerGradient  = "linear-gradient(135deg, #0284c7 0%, #0369a1 100%)";  // xanh dương
            subjectTitle    = "Đặt hàng thành công - Đơn hàng #" + orderId;
            statusText      = "Cảm ơn bạn đã tin tưởng Mini Seafood Shop! Đơn hàng #" + orderId
                            + " của bạn đã được tiếp nhận và đang chờ xác nhận thanh toán.";
            statusBadge     = "&#9203; Đang xử lý";   // ⏳
            badgeBg         = "#dbeafe";
            badgeColor      = "#1d4ed8";
            actionText      = "Xem Đơn Hàng";
        } else {
            icon            = "&#9989;";     // ✅
            headerGradient  = "linear-gradient(135deg, #16a34a 0%, #15803d 100%)";  // xanh lá
            subjectTitle    = "Thanh toán thành công - Đơn hàng #" + orderId;
            statusText      = "Tuyệt vời! Thanh toán đơn hàng #" + orderId
                            + " đã được xác nhận thành công. Chúng tôi sẽ sớm chuẩn bị và giao hàng cho bạn!";
            statusBadge     = "&#10003; Đã thanh toán"; // ✓
            badgeBg         = "#dcfce7";
            badgeColor      = "#15803d";
            actionText      = "Theo Dõi Đơn Hàng";
        }

        return template
                .replace("{{ICON}}",            icon)
                .replace("{{HEADER_GRADIENT}}", headerGradient)
                .replace("{{SUBJECT_TITLE}}",   subjectTitle)
                .replace("{{USER_NAME}}",       "bạn")
                .replace("{{ORDER_ID}}",        String.valueOf(orderId))
                .replace("{{STATUS_TEXT}}",     statusText)
                .replace("{{STATUS_BADGE}}",    statusBadge)
                .replace("{{BADGE_BG}}",        badgeBg)
                .replace("{{BADGE_COLOR}}",     badgeColor)
                .replace("{{ACTION_URL}}",      actionUrl != null ? actionUrl : "#")
                .replace("{{ACTION_TEXT}}",     actionText);
    }
}

