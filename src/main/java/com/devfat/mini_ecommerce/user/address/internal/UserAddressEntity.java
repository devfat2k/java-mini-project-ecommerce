package com.devfat.mini_ecommerce.user.address.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryEntity;
import com.devfat.mini_ecommerce.shared.base.BaseEntity;
import com.devfat.mini_ecommerce.user.internal.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_addresses")
public class UserAddressEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String ward;

    @Column(name = "address_detail", nullable = false, columnDefinition = "TEXT")
    private String addressDetail;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(nullable = false)
    private String tag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
