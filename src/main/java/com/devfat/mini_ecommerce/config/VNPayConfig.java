package com.devfat.mini_ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

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
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(cld.getTime());
    }

    public static String getExpireDate() {
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        // Cấu hình thời gian hết hạn là 15 phút
        cld.add(Calendar.MINUTE, 15);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(cld.getTime());
    }
}