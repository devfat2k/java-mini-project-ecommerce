package com.devfat.mini_ecommerce;

import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.security.JwtProvider;
import com.devfat.mini_ecommerce.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String SECRET_KEY =
            "VGhpc0lzQVNlY3JldEtleUZvckpXVFNUZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";


    @BeforeEach
    void setUp() throws Exception {

        jwtProvider = new JwtProvider();

        Field secretField = JwtProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtProvider, SECRET_KEY);

        Field expirationField = JwtProvider.class.getDeclaredField("jwtExpirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtProvider, 3600000L); // 1 hour
    }


    private UserPrincipal createUserPrincipal() {

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("password")
                .role(UserEntity.Role.USER)
                .isActive(true)
                .build();

        return new UserPrincipal(user);
    }


    @Test
    void generateToken_shouldCreateValidToken() {

        UserPrincipal userPrincipal = createUserPrincipal();

        String token = jwtProvider.generateToken(userPrincipal);

        assertNotNull(token);

        assertTrue(jwtProvider.validateToken(token));

        assertEquals(
                "test@gmail.com",
                jwtProvider.getEmailFromToken(token)
        );

        assertEquals(
                1L,
                jwtProvider.getUserIdFromToken(token)
        );

        assertEquals(
                UserEntity.Role.USER,
                jwtProvider.getRoleFromToken(token)
        );
    }


    @Test
    void validateToken_shouldReturnFalse_whenTokenInvalid() {

        String fakeToken = "fake.jwt.token";

        assertFalse(
                jwtProvider.validateToken(fakeToken)
        );
    }


    @Test
    void validateToken_shouldReturnFalse_whenTokenExpired() throws Exception {

        Field expirationField = JwtProvider.class.getDeclaredField("jwtExpirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtProvider, 1L);

        UserPrincipal userPrincipal = createUserPrincipal();

        String token = jwtProvider.generateToken(userPrincipal);

        Thread.sleep(20);

        assertFalse(
                jwtProvider.validateToken(token)
        );
    }
}