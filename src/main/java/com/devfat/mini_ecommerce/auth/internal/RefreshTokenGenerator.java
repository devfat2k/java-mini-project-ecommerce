package com.devfat.mini_ecommerce.auth.internal;











import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }
}
