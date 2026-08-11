-- ============================================================
-- MINI SHOP — Consolidated Schema V1 (Standardized Baseline)
-- Complete schema for Users, Roles, Categories, Products, Orders, Reviews, Banners, Payments
-- Dynamic validation compliant: every table has created_at, updated_at, created_by, updated_by
-- ============================================================

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
                       id             BIGSERIAL PRIMARY KEY,
                       full_name      VARCHAR(100) NOT NULL,
                       email          VARCHAR(150) UNIQUE NOT NULL,
                       phone_number   VARCHAR(15)  UNIQUE NOT NULL,
                       password       VARCHAR(255) NOT NULL,
                       is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                       email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
                       avatar_url     VARCHAR(500),
                       created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
                       created_by     VARCHAR(150),
                       updated_by     VARCHAR(150)
);

-- ============================================================
-- 2. ROLES
-- ============================================================
CREATE TABLE roles (
                      id             BIGSERIAL PRIMARY KEY,
                      name           VARCHAR(50) NOT NULL,
                      description    VARCHAR(255),
                      created_at     TIMESTAMP  NOT NULL DEFAULT NOW(),
                      updated_at     TIMESTAMP  NOT NULL DEFAULT NOW(),
                      created_by     VARCHAR(150),
                      updated_by     VARCHAR(150)
);

-- ============================================================
-- 3. PERMISSION
-- ============================================================
CREATE TABLE permissions (
                      id             BIGSERIAL PRIMARY KEY,
                      code           VARCHAR(100) UNIQUE NOT NULL, -- 'PRODUCT_CREATE', 'ORDER_UPDATE_STATUS'
                      description    VARCHAR(255),
                      created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
                      updated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
                      created_by     VARCHAR(150),
                      updated_by     VARCHAR(150)
);

-- ============================================================
-- 4. USER_ROLE - BẢNG TRUNG GIAN GIỮA QUAN HỆ N-N USER-ROLE
-- ============================================================
CREATE TABLE user_roles (
                        user_id       BIGINT REFERENCES users(id),
                        role_id       BIGINT REFERENCES roles(id),
                                      PRIMARY KEY (user_id, role_id),
                        created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
                        created_by    VARCHAR(150)
);

-- ============================================================
-- 5. ROLE_PERMISSION - BẢNG TRUNG GIAN QUAN HỆ N-N ROLE-PERMISSION
-- ============================================================
CREATE TABLE role_permissions (
                        role_id       BIGINT REFERENCES roles(id),
                        permission_id BIGINT REFERENCES permissions(id),
                        PRIMARY KEY (role_id, permission_id),
                        created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
                        created_by     VARCHAR(150)
);

-- ============================================================
-- 6. USER_ADDRESSES (Sổ địa chỉ giao hàng)
-- ============================================================
CREATE TABLE user_addresses (
                                id             BIGSERIAL PRIMARY KEY,
                                user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                recipient_name VARCHAR(100) NOT NULL,
                                phone          VARCHAR(15) NOT NULL,
                                province       VARCHAR(100) NOT NULL,
                                district       VARCHAR(100) NOT NULL,
                                ward           VARCHAR(100) NOT NULL,
                                address_detail TEXT NOT NULL,
                                is_default     BOOLEAN NOT NULL DEFAULT FALSE,
                                tag            VARCHAR(50),
                                created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
                                created_by     VARCHAR(150),
                                updated_by     VARCHAR(150)
);

-- ============================================================
-- 7. CATEGORIES (Standardized with Home Display Config)
-- ============================================================
CREATE TABLE categories (
                            id                 BIGSERIAL PRIMARY KEY,
                            name               VARCHAR(50) UNIQUE NOT NULL,
                            description        TEXT,
                            slug               VARCHAR(100) UNIQUE,
                            image_url          VARCHAR(500),
                            badge              VARCHAR(100),
                            badge_type         VARCHAR(20) CHECK (badge_type IN ('hot', 'number', 'fresh', 'dry')),
                            icon_name          VARCHAR(50),
                            home_display_style VARCHAR(10) CHECK (home_display_style IN ('main', 'card', 'icon')),
                            home_sort_order    INTEGER NOT NULL DEFAULT 0,
                            home_is_active     BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
                            created_by         VARCHAR(150),
                            updated_by         VARCHAR(150)
);

-- ============================================================
-- 8. PRODUCTS (Standardized with Rating, Search & Home Combo Fields)
-- ============================================================
CREATE TABLE products (
                          id               BIGSERIAL PRIMARY KEY,
                          name             VARCHAR(150) NOT NULL,
                          description      TEXT,
                          price            NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
                          stock            INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
                          category_id      BIGINT REFERENCES categories(id),
                          is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
                          image_url        VARCHAR(500),
                          unit             VARCHAR(20),
                          tags             TEXT[],
                          average_rating   NUMERIC(2, 1)  NOT NULL DEFAULT 0.0,
                          review_count     INTEGER        NOT NULL DEFAULT 0,
                          is_featured      BOOLEAN        NOT NULL DEFAULT FALSE,
                          original_price   NUMERIC(12, 2) CHECK (original_price >= 0),
                          spec             VARCHAR(255),
                          origin           VARCHAR(255),
                          weight_options   TEXT[],
                          product_type     VARCHAR(20)    NOT NULL DEFAULT 'REGULAR' CHECK (product_type IN ('REGULAR', 'COMBO')),
                          combo_category   VARCHAR(50),
                          combo_theme      VARCHAR(20)    CHECK (combo_theme IN ('light', 'dark')),
                          combo_tag        VARCHAR(50),
                          combo_cta_text   VARCHAR(100),
                          combo_href       VARCHAR(255),
                          is_breakout      BOOLEAN        NOT NULL DEFAULT FALSE,
                          combo_sort_order INTEGER        NOT NULL DEFAULT 0,
                          version          INTEGER        NOT NULL DEFAULT 0,
                          created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
                          updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
                          created_by       VARCHAR(150),
                          updated_by       VARCHAR(150)
);

-- ============================================================
-- 9. ORDERS
-- ============================================================
CREATE TABLE orders (
                        id                        BIGSERIAL PRIMARY KEY,
                        user_id                   BIGINT NOT NULL REFERENCES users(id),
                        shipping_address_id       BIGINT REFERENCES user_addresses(id) ON DELETE SET NULL,
                        shipping_address_snapshot TEXT,
                        payment_method            VARCHAR(20) NOT NULL DEFAULT 'VNPAY' CHECK (payment_method IN ('VNPAY', 'COD', 'MOMO', 'ZALOPAY')),
                        status                    VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DONE', 'CANCELLED')),
                        total_amount              NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
                        note                      TEXT,
                        version                   INTEGER NOT NULL DEFAULT 0,
                        created_at                TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at                TIMESTAMP NOT NULL DEFAULT NOW(),
                        created_by                VARCHAR(150),
                        updated_by                VARCHAR(150)
);

-- ============================================================
-- 10. ORDER_STATUS_HISTORY
-- ============================================================
CREATE TABLE order_status_history (
                                      id         BIGSERIAL PRIMARY KEY,
                                      order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                      status     VARCHAR(20) NOT NULL,
                                      note       TEXT,
                                      changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                      changed_by VARCHAR(150)
);

-- ============================================================
-- 11. ORDER_ITEMS
-- ============================================================
CREATE TABLE order_items (
                             id         BIGSERIAL PRIMARY KEY,
                             order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL REFERENCES products(id),
                             quantity   INTEGER        NOT NULL CHECK (quantity > 0),
                             unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             created_by VARCHAR(150),
                             updated_by VARCHAR(150)
);

-- ============================================================
-- 12. PRODUCT_REVIEWS (Standardized with Home Featured Flag)
-- ============================================================
CREATE TABLE product_reviews (
                                 id               BIGSERIAL PRIMARY KEY,
                                 product_id       BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                 user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 order_id         BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                 rating           SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                 comment          TEXT,
                                 is_visible       BOOLEAN NOT NULL DEFAULT TRUE,
                                 is_featured_home BOOLEAN NOT NULL DEFAULT FALSE,
                                 created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
                                 created_by       VARCHAR(150),
                                 updated_by       VARCHAR(150),
                                 CONSTRAINT uk_user_order_product_review UNIQUE (user_id, order_id, product_id)
);

-- ============================================================
-- 13. REFRESH_TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
                                id         BIGSERIAL PRIMARY KEY,
                                user_id    BIGINT NOT NULL REFERENCES users(id),
                                token_hash VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked    BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                created_by VARCHAR(150),
                                updated_by VARCHAR(150)
);

-- ============================================================
-- 14. PAYMENTS
-- ============================================================
CREATE TABLE payments (
                          id                      BIGSERIAL PRIMARY KEY,
                          order_id                BIGINT NOT NULL REFERENCES orders(id),
                          amount                  NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
                          provider                VARCHAR(20) NOT NULL CHECK (provider IN ('VNPAY', 'MOMO', 'ZALOPAY', 'ACB', 'VCB')),
                          payment_method          VARCHAR(20) NOT NULL CHECK (payment_method IN ('BANK', 'WALLET', 'CASH')),
                          status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED')),
                          provider_transaction_id VARCHAR(100) UNIQUE,
                          paid_at                 TIMESTAMP,
                          created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
                          created_by              VARCHAR(150),
                          updated_by              VARCHAR(150)
);

-- ============================================================
-- 15. OTP_VERIFICATIONS
-- ============================================================
CREATE TABLE otp_verifications (
                                   id         BIGSERIAL PRIMARY KEY,
                                   user_id    BIGINT NOT NULL REFERENCES users(id),
                                   otp_hash   VARCHAR(255) NOT NULL,
                                   purpose    VARCHAR(150) NOT NULL CHECK (purpose IN ('REGISTER_VERIFICATION', 'RESET_PASSWORD', 'CHANGE_PASSWORD_CONFIRMATION')),
                                   expires_at TIMESTAMP NOT NULL,
                                   attempts   INTEGER NOT NULL DEFAULT 0,
                                   consumed   BOOLEAN NOT NULL DEFAULT FALSE,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   created_by VARCHAR(150),
                                   updated_by VARCHAR(150)
);

-- ============================================================
-- 16. INDEXES & COMPOSITE SEARCH INDEXES
-- ============================================================
CREATE INDEX idx_products_category         ON products(category_id);
CREATE INDEX idx_products_status_category  ON products(is_active, category_id);
CREATE INDEX idx_products_price            ON products(price);
CREATE INDEX idx_products_created_at       ON products(created_at DESC);
CREATE INDEX idx_orders_user               ON orders(user_id);
CREATE INDEX idx_orders_status             ON orders(status);
CREATE INDEX idx_orders_shipping_addr      ON orders(shipping_address_id);
CREATE INDEX idx_order_items_order         ON order_items(order_id);
CREATE INDEX idx_order_items_product       ON order_items(product_id);
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_payments_order            ON payments(order_id);
CREATE INDEX idx_payments_status           ON payments(status);
CREATE INDEX idx_otp_user_purpose          ON otp_verifications(user_id, purpose);
CREATE INDEX idx_user_addresses_user       ON user_addresses(user_id);
CREATE INDEX idx_order_status_hist_order   ON order_status_history(order_id);
CREATE INDEX idx_product_reviews_product   ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_user      ON product_reviews(user_id);
CREATE INDEX idx_product_reviews_featured  ON product_reviews(is_featured_home) WHERE is_featured_home = TRUE;