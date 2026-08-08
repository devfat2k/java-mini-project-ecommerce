package com.devfat.mini_ecommerce.shared.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    RateLimitType type() default RateLimitType.PUBLIC_API;

    boolean byIp() default true;
}
