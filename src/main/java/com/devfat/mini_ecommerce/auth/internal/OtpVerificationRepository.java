package com.devfat.mini_ecommerce.auth.internal;

import com.devfat.mini_ecommerce.auth.OtpPurpose;










import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerificationEntity, Long> {
    Optional<OtpVerificationEntity> findFirstByUser_IdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            Long userId, OtpPurpose purpose
    );

    @Modifying
    @Query("DELETE FROM OtpVerificationEntity o WHERE o.expiresAt < :cutoff")
    void deleteAllExpiredBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE OtpVerificationEntity o SET o.consumed = true " +
           "WHERE o.user.id = :userId AND o.purpose = :purpose AND o.consumed = false")
    void consumeAllPending(@Param("userId") Long userId, @Param("purpose") OtpPurpose purpose);
}
