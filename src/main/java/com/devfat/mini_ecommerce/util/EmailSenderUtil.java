package com.devfat.mini_ecommerce.util;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSenderUtil {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailHost;

    public void sendTextEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailHost);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            javaMailSender.send(message);
            log.info("Gửi email text thành công tới {}", to);
        } catch (Exception e) {
            log.error("Gửi email text thất bại tới {}, lỗi: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Resource resource = new ClassPathResource("templates/email/SendEmailTemplate.html");
            String htmlContent = new String(resource.getInputStream().readAllBytes());

            helper.setFrom(emailHost);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("Gửi email HTML thành công tới {}", to);
        } catch (Exception e) {
            log.error("Gửi email HTML thất bại tới {}, lỗi: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
