package com.devfat.mini_ecommerce.security;

import com.devfat.mini_ecommerce.enums.Role;
import com.devfat.mini_ecommerce.exception.InvalidActionTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtProvider {

    // Secret key và thời gian sống của JWT được lấy từ application.yml
    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    /*
     * Tạo SecretKey dùng để ký và xác thực JWT.
     * Flow:
     * 1. Lấy secret key dạng String từ config.
     * 2. Decode Base64 thành byte[].
     * 3. Tạo SecretKey theo thuật toán HMAC để:
     *    - sign JWT khi generate token.
     *    - verify chữ ký khi validate token.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /*
     * Generate JWT token sau khi user login thành công.
     * Flow:
     * User login
     *      |
     * AuthenticationManager xác thực username/password
     *      |
     * Tạo JWT chứa thông tin user
     *      |
     * Client lưu token và gửi lại trong Authorization header
     * JWT gồm 3 phần:
     * Header.Payload.Signature
     * Payload chứa:
     * - subject  : email của user
     * - userId   : id user
     * - role     : quyền user
     * - issuedAt : thời điểm tạo token
     * - expiration: thời điểm hết hạn
     */
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        // Custom claims: dữ liệu riêng muốn lưu trong JWT payload
        claims.put("userId", userPrincipal.getUserId());
        claims.put("role", userPrincipal.getRole());
        return Jwts.builder()
                // Thêm custom claims vào payload
                .claims(claims)
                // subject là dữ liệu định danh chính của user
                // Trong project này subject = email
                .subject(userPrincipal.getUsername())
                // Thời gian tạo token
                .issuedAt(now)
                // Thời gian hết hạn token
                .expiration(expiryDate)
                // Ký token bằng SecretKey
                // Giúp server biết token có bị chỉnh sửa hay không
                .signWith(getSigningKey())
                // Convert JWT object thành String để trả về client
                .compact();
    }
    /*
     * Kiểm tra JWT có hợp lệ hay không.
     * Flow:
     * Request gửi JWT lên
     * Lấy token
     * Dùng SecretKey parse token
     * Nếu:
     * - chữ ký đúng
     * - token chưa hết hạn
     * - format đúng
     * => return true
     * Nếu lỗi:
     * => catch exception và return false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    // Dùng secret key để kiểm tra chữ ký JWT
                    .verifyWith(getSigningKey())
                    .build()
                    // Parse token và kiểm tra:
                    // - signature
                    // - expiration
                    // - claims
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
    /*
     * Lấy email từ JWT.
     * Generate token:
     * subject(email)
     * Payload JWT
     * Method này đọc lại subject từ payload.
     * Dùng trong JwtAuthenticationFilter:
     * Token hợp lệ
     * lấy email
     * load UserDetails từ database
     */
    public String getEmailFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        Claims payload = jws.getPayload();
        // subject đã set ở generateToken()
        return payload.getSubject();
    }
    /*
     * Lấy userId từ JWT claim.
     * Generate:
     * claims.put("userId", id)
     * Read:
     * payload.get("userId")
     */
    public Long getUserIdFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        Claims payload = jws.getPayload();
        return payload.get("userId", Long.class);
    }
    /*
     * Lấy role từ JWT claim.
     * Generate:
     * claims.put("role", role)
     * Read:
     * payload.get("role")
     * Sau đó convert String -> Enum Role
     */
    public Role getRoleFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        Claims payload = jws.getPayload();
        String role = payload.get("role", String.class);
        return Role.valueOf(role);
    }
    /*
     * Trả về thời gian sống của JWT.
     * Dùng khi cần kiểm tra hoặc cấu hình refresh token sau này.
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }



    public String generateActionToken(Long userId, String scope, Duration ttl) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl.toMillis());
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long validateActionTokenAndGetUserId(String token, String expectedScope) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        Claims payload = jws.getPayload();
        String actualScope = payload.get("scope", String.class);
        if (!expectedScope.equals(actualScope)) {
            throw new InvalidActionTokenException("Token scope không hợp lệ cho hành động này");
        }
        return Long.valueOf(payload.getSubject());
    }
}