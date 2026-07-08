package com.devfat.mini_ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtProvider {

    @Value("${app.security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        // Build token với: subject = email, claim "userId", claim "role", iat, exp
        // Dùng Jwts.builder() của JJWT — tự tra doc để viết đúng method chain
        claims.put("userId", userPrincipal.getUserId());
        claims.put("roles", userPrincipal.getAuthorities());

        return Jwts.builder()
                .claims(claims)
                /**
                 *  public String getUsername() {
                 *         return userEntity.getEmail();
                 *     }
                 */
                .subject(userPrincipal.getUsername()) // -> Lưu ý: username đang là email
                .issuedAt(now)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        // Thử parse token bằng secret key
        // Nếu ném exception (sai chữ ký, hết hạn, format sai...) -> return false
        // Nếu parse thành công -> return true
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims are empty or invalid: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        Claims payload = jws.getPayload();
        return payload.getSubject();   // subject = email, đúng theo cách generateToken() đã set
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        Claims payload = jws.getPayload();
        return payload.get("userId", Long.class);   // đọc đúng claim "userId" đã set ở generateToken()
    }
}
