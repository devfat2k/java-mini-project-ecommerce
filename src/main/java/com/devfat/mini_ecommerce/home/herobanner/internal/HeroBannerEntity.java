package com.devfat.mini_ecommerce.home.herobanner.internal;

import com.devfat.mini_ecommerce.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.math.BigDecimal;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@Table(name = "hero_banners")
public class HeroBannerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "badge_text", length = 100)
    private String badgeText;

    @Column(name = "badge_icon", length = 50)
    private String badgeIcon;

    @Column(name = "title_prefix", length = 100)
    private String titlePrefix;

    @Column(name = "title_highlight", length = 100)
    private String titleHighlight;

    @Column(name = "title_suffix", length = 100)
    private String titleSuffix;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "primary_cta_label", length = 100)
    private String primaryCtaLabel;

    @Column(name = "primary_cta_href", length = 255)
    private String primaryCtaHref;

    @Column(name = "primary_cta_icon", length = 50)
    private String primaryCtaIcon;

    @Column(name = "secondary_cta_label", length = 100)
    private String secondaryCtaLabel;

    @Column(name = "secondary_cta_href", length = 255)
    private String secondaryCtaHref;

    @Column(name = "secondary_cta_icon", length = 50)
    private String secondaryCtaIcon;

    @Column(name = "card_image_url", length = 500)
    private String cardImageUrl;

    @Column(name = "card_image_alt", length = 255)
    private String cardImageAlt;

    @Column(name = "card_combo_badge", length = 100)
    private String cardComboBadge;

    @Column(name = "card_discount_badge", length = 50)
    private String cardDiscountBadge;

    @Column(name = "card_original_price", precision = 12, scale = 2)
    private BigDecimal cardOriginalPrice;

    @Column(name = "card_sale_price", precision = 12, scale = 2)
    private BigDecimal cardSalePrice;

    @Column(name = "card_title", length = 150)
    private String cardTitle;

    @Column(name = "card_subtitle", length = 255)
    private String cardSubtitle;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HeroBannerEntity that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
