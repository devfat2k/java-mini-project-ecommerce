package com.devfat.mini_ecommerce.category.internal;

import com.devfat.mini_ecommerce.product.internal.ProductEntity;
import com.devfat.mini_ecommerce.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.util.List;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 100)
    private String slug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String badge; // Ví dụ: "TOP 1", "HOT", "TƯƠI SỐNG"

    @Column(name = "badge_type", length = 20)
    private String badgeType; // Giá trị: "hot" | "number" | "fresh" | "dry"

    @Column(name = "icon_name", length = 50)
    private String iconName; // Tên icon Lucide cho FE (ví dụ: "fish", "utensils")

    @Column(name = "home_display_style", length = 10)
    private String homeDisplayStyle; // Style hiển thị FE: "main" | "card" | "icon"

    @Builder.Default
    @Column(name = "home_sort_order", nullable = false)
    private Integer homeSortOrder = 0; // Thứ tự ưu tiên sắp xếp trên trang chủ

    @Builder.Default
    @Column(name = "home_is_active", nullable = false)
    private boolean homeIsActive = false; // Bật/tắt hiển thị category này trên trang chủ


    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<ProductEntity> products;

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
