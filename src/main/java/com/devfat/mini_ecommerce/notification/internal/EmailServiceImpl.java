package com.devfat.mini_ecommerce.notification.internal;

import com.devfat.mini_ecommerce.notification.EmailService;
import com.devfat.mini_ecommerce.notification.dto.EmailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MailTransport mailTransport;
    private final EmailTemplateHelper emailTemplateHelper;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
            String actionUrl = baseUrl + "/en/orders/" + orderId;
            String html = emailTemplateHelper.buildOrderEmailHtml(OrderEmailType.ORDER_PLACED, orderId, actionUrl);
            mailTransport.sendHtmlEmail(
                    toEmail,
                    "Đặt hàng thành công - Đơn hàng #" + orderId,
                    html
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
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
            String actionUrl = baseUrl + "/en/orders/" + orderId;
            String html = emailTemplateHelper.buildOrderEmailHtml(OrderEmailType.PAYMENT_SUCCESS, orderId, actionUrl);
            mailTransport.sendHtmlEmail(
                    toEmail,
                    "Thanh toán thành công - Đơn hàng #" + orderId,
                    html
            );
        } catch (Exception e) {
            log.error("Gửi email thanh toán đơn {} thất bại: {}", orderId, e.getMessage());
        }
    }
}
