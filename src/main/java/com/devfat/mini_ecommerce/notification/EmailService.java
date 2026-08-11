package com.devfat.mini_ecommerce.notification;

import com.devfat.mini_ecommerce.notification.dto.EmailRequestDto;


public interface EmailService {
    void sendTextEmail(EmailRequestDto request);
    void sendHtmlEmail(EmailRequestDto request);
    void sendAttachmentEmail(EmailRequestDto request);
    void sendOrderConfirmation(String toEmail, Long orderId);
    void sendPaymentSuccessEmail(String toEmail, Long orderId);
}
