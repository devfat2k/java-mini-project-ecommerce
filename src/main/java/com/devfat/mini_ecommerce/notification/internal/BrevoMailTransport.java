package com.devfat.mini_ecommerce.notification.internal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class BrevoMailTransport implements MailTransport {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.mail.brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.mail.brevo.sender-email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendTextEmail(String to, String subject, String content) {
        sendViaApi(to, subject, "<pre>" + content + "</pre>"); // Brevo chỉ nhận htmlContent, bọc tạm thẻ pre cho text thuần
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        sendViaApi(to, subject, content);
    }

    private void sendViaApi(String to, String subject, String htmlContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        String maskedKey = (brevoApiKey != null && brevoApiKey.length() > 10)
                ? brevoApiKey.substring(0, 6) + "..." + brevoApiKey.substring(brevoApiKey.length() - 4)
                : "NULL/EMPTY";
        log.info("Gửi email qua Brevo API với sender: {}, apiKey: {}", senderEmail, maskedKey);

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", senderEmail, "name", "Mini Ecommerce"),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);
            log.info("Gửi email (Brevo API) thành công tới {}", to);
        } catch (RestClientException e) {
            log.error("Gửi email (Brevo API) thất bại tới {}, lỗi: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
