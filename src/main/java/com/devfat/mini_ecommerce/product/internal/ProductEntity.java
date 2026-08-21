package com.devfat.mini_ecommerce.product.internal;

import com.devfat.mini_ecommerce.category.internal.CategoryEntity;
import com.devfat.mini_ecommerce.order.internal.OrderItemEntity;
import com.devfat.mini_ecommerce.shared.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
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


    @Column(nullable = false)
    private String unit;

    @Column(name = "tags", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> tags;


    @OneToMany(mappedBy = "product")
    private List<OrderItemEntity> orderItems;


    @Column(name = "average_rating")
    private BigDecimal averageRating;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "original_price", precision = 12, scale = 2)
    BigDecimal originalPrice;

    private String spec;

    private String origin;

    @Column(name = "weight_options", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> weightOptions;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType = ProductType.REGULAR;

    @Column(name = "combo_category", length = 50)
    private String comboCategory;

    @Column(name = "combo_theme", length = 20)
    private String comboTheme;

    @Column(name = "combo_tag", length = 50)
    private String comboTag;

    @Column(name = "combo_cta_text", length = 100)
    private String comboCtaText;

    @Column(name = "combo_href")
    private String comboHref;

    @Builder.Default
    @Column(name = "is_breakout", nullable = false)
    private boolean isBreakout = false;

    @Builder.Default
    @Column(name = "combo_sort_order", nullable = false)
    private Integer comboSortOrder = 0;


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
        return getClass().hashCode();
    }
}
