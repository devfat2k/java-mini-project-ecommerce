package com.devfat.mini_ecommerce.shared.ratelimit;

import com.devfat.mini_ecommerce.shared.exception.TooManyRequestsException;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitingAspect {

    private final RateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String identifierKey;
        if (rateLimit.byIp()) {
            String clientIp = extractClientIp(request);
            identifierKey = "rate_limit:" + rateLimit.type().name() + ":ip:" + clientIp;
        } else {
            Long userId = getCurrentUserId();
            if (userId != null) {
                identifierKey = "rate_limit:" + rateLimit.type().name() + ":user:" + userId;
            } else {
                identifierKey = "rate_limit:" + rateLimit.type().name() + ":ip:" + extractClientIp(request);
            }
        }

        ConsumptionProbe probe = rateLimiterService.tryConsume(identifierKey, rateLimit.type());

        if (probe.isConsumed()) {
            return joinPoint.proceed();
        } else {
            long waitForRefillNanos = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = (long) Math.ceil((double) waitForRefillNanos / 1_000_000_000.0);
            if (retryAfterSeconds < 1) {
                retryAfterSeconds = 1;
            }

            if (response != null) {
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));
            }

            log.warn("Rate limit exceeded for key: {}. Retry after {}s", identifierKey, retryAfterSeconds);

            throw new TooManyRequestsException(
                    "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau " + retryAfterSeconds + " giây.",
                    retryAfterSeconds
            );
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }
}
