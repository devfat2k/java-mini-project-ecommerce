package com.devfat.mini_ecommerce.payment.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    private String payUrl;
    private String tmnCode;
    private String secretKey;
    private String returnUrl;
    private String ipnUrl;

    public static String getCreateDate() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String getExpireDate() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .plusMinutes(15)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
