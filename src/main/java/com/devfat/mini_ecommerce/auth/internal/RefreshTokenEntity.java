package com.devfat.mini_ecommerce.auth.internal;

import com.devfat.mini_ecommerce.shared.base.BaseEntity;
import com.devfat.mini_ecommerce.user.internal.UserEntity;










import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expireAt;

    @Column(nullable = false)
    private boolean revoked;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshTokenEntity that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // constant hashCode — đúng theo Vlad
    }
}
