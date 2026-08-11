package com.devfat.mini_ecommerce.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final LettuceBasedProxyManager<byte[]> proxyManager;
    private final RateLimitProperties rateLimitProperties;

    public ConsumptionProbe tryConsume(String key, RateLimitType type) {
        Bandwidth bandwidth = resolveBandwidth(type);

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        Bucket bucket = proxyManager.builder().build(keyBytes, configSupplier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.debug("RateLimit token consumed for key: {}. Remaining: {}", key, probe.getRemainingTokens());
        } else {
            log.warn("RateLimit exceeded for key: {}. Nanos to wait: {}", key, probe.getNanosToWaitForRefill());
        }

        return probe;
    }

    private Bandwidth resolveBandwidth(RateLimitType type) {
        String ruleKey = type.name().toLowerCase().replace("_", "-");
        RateLimitProperties.RuleConfig ruleConfig = rateLimitProperties.getRules().get(ruleKey);

        if (ruleConfig != null) {
            log.debug("Using YAML config for rule '{}': capacity={}, refill={}, duration={}m",
                    ruleKey, ruleConfig.getCapacity(), ruleConfig.getRefillTokens(), ruleConfig.getDurationMinutes());
            return Bandwidth.builder()
                    .capacity(ruleConfig.getCapacity())
                    .refillIntervally(ruleConfig.getRefillTokens(), Duration.ofMinutes(ruleConfig.getDurationMinutes()))
                    .build();
        }

        log.warn("No YAML config found for rule '{}', falling back to hardcoded default in enum", ruleKey);
        return Bandwidth.builder()
                .capacity(type.getCapacity())
                .refillIntervally(type.getRefillTokens(), type.getRefillDuration())
                .build();
    }
}
