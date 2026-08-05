package com.devfat.mini_ecommerce.entity;


import com.devfat.mini_ecommerce.base.BaseEntity;
import com.devfat.mini_ecommerce.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
@Table(name = "otp_verifications")
public class OtpVerificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 150)
    private OtpPurpose purpose;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // constant hashCode — đúng theo Vlad
    }
}
