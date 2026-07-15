package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.user.id = :userId AND (r.revoked = true OR r.expireAt < :now)")
    void deleteExpiredOrRevokedByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
