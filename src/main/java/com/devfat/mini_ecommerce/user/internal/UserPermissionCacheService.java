package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.devfat.mini_ecommerce.user.dto.UserPermissionCacheDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPermissionCacheService {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Lấy danh sách Role & Permission của User.
     * Nếu đã có trong Redis (Key: user-permissions::[userId]), lấy trực tiếp từ Redis.
     * Nếu chưa có, thực thi logic bên trong (query DB) rồi tự động lưu kết quả vào Redis.
     */
    @Cacheable(value = "user-permissions", key = "#userId")
    public UserPermissionCacheDto getUserPermissions(Long userId, String email) {
        log.info("Cache miss for userId: {}. Fetching permissions from DB...", userId);

        // Gọi CustomUserDetailsService để load UserPrincipal chứa roles & permissions từ DB
        var userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(email);

        Set<String> roles = userPrincipal.getRoles() != null ? userPrincipal.getRoles() : new HashSet<>();
        Set<String> permissions = userPrincipal.getPermissions() != null ? userPrincipal.getPermissions() : new HashSet<>();

        return UserPermissionCacheDto.builder()
                .userId(userId)
                .email(email)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    /**
     * Xóa cache của 1 User cụ thể khi Admin cập nhật Role cho User đó.
     */
    @CacheEvict(value = "user-permissions", key = "#userId")
    public void evictUserPermissions(Long userId) {
        log.info("Evicted user-permissions cache for userId: {}", userId);
    }

    /**
     * Xóa TOÀN BỘ cache phân quyền khi Admin thay đổi danh sách Permission của một Role.
     */
    @CacheEvict(value = "user-permissions", allEntries = true)
    public void evictAllUserPermissions() {
        log.info("Evicted ALL user-permissions cache due to Role-Permission update.");
    }
}
