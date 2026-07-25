package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.EmailRequestDto;
import com.devfat.mini_ecommerce.service.EmailService;
import com.devfat.mini_ecommerce.util.MailTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MailTransport mailTransport;

    @Override
    @Async("emailTaskExecutor")
    public void sendTextEmail(EmailRequestDto request) {
        log.info("Gửi text email tới {}, thread: {}", request.getTo(), Thread.currentThread().getName());
        try {
            mailTransport.sendTextEmail(request.getTo(), request.getSubject(), request.getMessageBody());
        } catch (Exception e) {
            log.error("Gửi text email thất bại tới {}: {}", request.getTo(), e.getMessage());
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendHtmlEmail(EmailRequestDto request) {
        log.info("Gửi HTML email tới {}, thread: {}", request.getTo(), Thread.currentThread().getName());
        try {
            mailTransport.sendHtmlEmail(request.getTo(), request.getSubject(), request.getMessageBody());
        } catch (Exception e) {
            log.error("Gửi HTML email thất bại tới {}: {}", request.getTo(), e.getMessage());
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendAttachmentEmail(EmailRequestDto request) {
        throw new UnsupportedOperationException("sendAttachmentEmail chưa được triển khai");
    }


    @Override
    @Async("emailTaskExecutor")
    public void sendOrderConfirmation(String toEmail, Long orderId) {
        log.info("Bắt đầu gửi email xác nhận đơn {}, thread: {}", orderId, Thread.currentThread().getName());
        try {
            mailTransport.sendTextEmail(
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
            mailTransport.sendTextEmail(
                    toEmail,
                    "Thanh toán thành công - Đơn hàng #" + orderId,
                    "Đơn hàng #" + orderId + " đã được thanh toán thành công. Chúng tôi sẽ sớm giao hàng!"
            );
        } catch (Exception e) {
            log.error("Gửi email thanh toán đơn {} thất bại: {}", orderId, e.getMessage());
        }
    }
}
