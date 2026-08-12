-- ============================================================
-- 13. HERO_BANNERS (Standardized 23 Columns for Home Hero Section)
-- ============================================================
CREATE TABLE hero_banners (
                              id                  BIGSERIAL PRIMARY KEY,
                              sort_order          INTEGER NOT NULL DEFAULT 0,
                              is_active           BOOLEAN NOT NULL DEFAULT TRUE,
                              badge_text          VARCHAR(100),
                              badge_icon          VARCHAR(50),
                              title_prefix        VARCHAR(100),
                              title_highlight     VARCHAR(100),
                              title_suffix        VARCHAR(100),
                              description         TEXT,
                              primary_cta_label   VARCHAR(100),
                              primary_cta_href    VARCHAR(255),
                              primary_cta_icon    VARCHAR(50),
                              secondary_cta_label VARCHAR(100),
                              secondary_cta_href  VARCHAR(255),
                              secondary_cta_icon  VARCHAR(50),
                              card_image_url      VARCHAR(500),
                              card_image_alt      VARCHAR(255),
                              card_combo_badge    VARCHAR(100),
                              card_discount_badge VARCHAR(50),
                              card_original_price NUMERIC(12, 2),
                              card_sale_price      NUMERIC(12, 2),
                              card_title          VARCHAR(150),
                              card_subtitle       VARCHAR(255),
                              created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                              created_by          VARCHAR(150),
                              updated_by          VARCHAR(150)
);

-- ============================================================
-- 14. DAILY_ARRIVALS (Daily Seafood Story Picks)
-- ============================================================
CREATE TABLE daily_arrivals (
                                id           BIGSERIAL PRIMARY KEY,
                                product_id   BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                arrival_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                arrived_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                                badge        VARCHAR(100),
                                title        VARCHAR(150),
                                description  TEXT,
                                weight       VARCHAR(100),
                                origin       VARCHAR(255),
                                is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                                created_by   VARCHAR(150),
                                updated_by   VARCHAR(150),
                                CONSTRAINT uk_daily_arrival_product_date UNIQUE (product_id, arrival_date)
);

CREATE INDEX idx_hero_banners_active    ON hero_banners(is_active, sort_order);
CREATE INDEX idx_daily_arrivals_date    ON daily_arrivals(arrival_date, is_active);
