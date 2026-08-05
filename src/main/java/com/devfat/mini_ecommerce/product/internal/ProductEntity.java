package com.devfat.mini_ecommerce.product.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryEntity;
import com.devfat.mini_ecommerce.order.internal.OrderItemEntity;
import com.devfat.mini_ecommerce.shared.base.BaseEntity;










import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@Table(name = "products")
public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "product")
    private List<OrderItemEntity> orderItems;



    @Version
    private Integer version;


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
