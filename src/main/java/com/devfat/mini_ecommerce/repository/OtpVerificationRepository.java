package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.OtpVerificationEntity;
import com.devfat.mini_ecommerce.enums.OtpPurpose;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerificationEntity, Long> {
    Optional<OtpVerificationEntity> findFirstByUser_IdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            Long userId, OtpPurpose purpose
    );

    @Modifying
    @Query("DELETE FROM OtpVerificationEntity o WHERE o.expiresAt < :cutoff")
    void deleteAllExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
