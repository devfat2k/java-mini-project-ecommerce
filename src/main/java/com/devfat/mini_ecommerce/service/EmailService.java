package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.EmailRequestDto;

public interface EmailService {
    void sendTextEmail(EmailRequestDto request);
    void sendHtmlEmail(EmailRequestDto request);
    void sendAttachmentEmail(EmailRequestDto request);
    void sendOrderConfirmation(String toEmail, Long orderId);
    void sendPaymentSuccessEmail(String toEmail, Long orderId);
}
