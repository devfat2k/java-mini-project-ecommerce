package com.devfat.mini_ecommerce.notification.internal;



public interface MailTransport {
    void sendTextEmail(String to, String subject, String content);
    void sendHtmlEmail(String to, String subject, String content);
}
