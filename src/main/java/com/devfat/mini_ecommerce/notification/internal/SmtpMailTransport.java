package com.devfat.mini_ecommerce.notification.internal;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Profile("smtp") // chỉ active khi SPRING_PROFILES_ACTIVE=smtp
@RequiredArgsConstructor
@Slf4j
public class SmtpMailTransport implements MailTransport {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.brevo.sender-email}")
    private String senderEmail;

    @Override
    public void sendTextEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            javaMailSender.send(message);
            log.info("Gửi email text (SMTP) thành công tới {}", to);
        } catch (Exception e) {
            log.error("Gửi email text (SMTP) thất bại tới {}, lỗi: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            javaMailSender.send(message);
            log.info("Gửi email HTML (SMTP) thành công tới {}", to);
        } catch (Exception e) {
            log.error("Gửi email HTML (SMTP) thất bại tới {}, lỗi: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
