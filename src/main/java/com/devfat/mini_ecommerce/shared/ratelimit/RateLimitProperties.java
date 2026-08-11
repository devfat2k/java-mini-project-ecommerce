package com.devfat.mini_ecommerce.shared.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private Map<String, RuleConfig> rules = new HashMap<>();

    @Getter
    @Setter
    public static class RuleConfig {
        private long capacity;
        private long refillTokens;
        private long durationMinutes;
    }
}
