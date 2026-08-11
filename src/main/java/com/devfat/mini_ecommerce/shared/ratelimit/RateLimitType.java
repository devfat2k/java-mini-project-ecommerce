package com.devfat.mini_ecommerce.shared.ratelimit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum RateLimitType {
    LOGIN(5, 5, Duration.ofMinutes(15)),
    OTP(3, 3, Duration.ofMinutes(5)),
    PUBLIC_API(60, 60, Duration.ofMinutes(1)),
    USER_ACTION(30, 30, Duration.ofMinutes(1));

    private final long capacity;
    private final long refillTokens;
    private final Duration refillDuration;
}