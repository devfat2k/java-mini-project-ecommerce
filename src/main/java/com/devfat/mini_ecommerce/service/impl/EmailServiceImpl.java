package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.entity.EmailEntity;
import com.devfat.mini_ecommerce.service.EmailService;
import com.devfat.mini_ecommerce.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final String EMAIL_HOST = "phathn2688@gmail.com";
    private final EmailSenderUtil emailSenderUtil;

    @Override
    public String sendTextEmail(EmailEntity email) {
        return "";
    }

    @Override
    public String sendHtmlEmail(EmailEntity email) {
        return "";
    }

    @Override
    public String sendAttachmentEmail(EmailEntity email) {
        return "";
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendOrderConfirmation(String toEmail, Long orderId) {
        log.info("Bắt đầu gửi email xác nhận đơn {}, thread: {}", orderId, Thread.currentThread().getName());
        try {
            emailSenderUtil.sendTextEmail(
                    toEmail,
                    "Xác nhận đơn hàng #" + orderId,
                    "Cảm ơn bạn đã đặt hàng! Đơn hàng #" + orderId + " đang được xử lý."
            );
        } catch (Exception e) {
            log.error("Gửi email xác nhận đơn hàng {} thất bại: {}", orderId, e.getMessage());
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPaymentSuccessEmail(String toEmail, Long orderId) {
        log.info("Bắt đầu gửi email thanh toán đơn {}, thread: {}", orderId, Thread.currentThread().getName());
        try {
            emailSenderUtil.sendTextEmail(
                    toEmail,
                    "Thanh toán thành công - Đơn hàng #" + orderId,
                    "Đơn hàng #" + orderId + " đã được thanh toán thành công. Chúng tôi sẽ sớm giao hàng!"
            );
        } catch (Exception e) {
            log.error("Gửi email thanh toán đơn {} thất bại: {}", orderId, e.getMessage());
        }
    }
}
