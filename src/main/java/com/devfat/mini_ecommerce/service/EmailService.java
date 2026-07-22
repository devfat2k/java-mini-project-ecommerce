package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.entity.EmailEntity;

public interface EmailService {
    String sendTextEmail(EmailEntity email);
    String sendHtmlEmail(EmailEntity email);
    String sendAttachmentEmail(EmailEntity email);
    void sendOrderConfirmation(String toEmail, Long orderId);
    void sendPaymentSuccessEmail(String toEmail, Long orderId);
}
