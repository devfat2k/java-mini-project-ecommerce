-- ============================================================
-- MINI SHOP — Consolidated Schema V1
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
-- 2. ROLE
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
-- 4. USER_ROLE - BẢNG TRUNG GIAN GIỮA quan hệ hệ nhiều - nhiều của USER-ROLE
-- ============================================================
CREATE TABLE user_roles (
                        user_id       BIGINT REFERENCES users(id),
                        role_id       BIGINT REFERENCES roles(id),
                                      PRIMARY KEY (user_id, role_id),
                        created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
                        created_by    VARCHAR(150)
);

-- ============================================================
-- 5. ROLE_PERMISSION -- BẢNG TRUNG GIAN QUAN HỆ N-N
-- ============================================================
CREATE TABLE role_permissions (
                        role_id       BIGINT REFERENCES roles(id),
                        permission_id BIGINT REFERENCES permissions(id),
                        PRIMARY KEY (role_id, permission_id),
                        created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
                        created_by     VARCHAR(150)
);
-- ============================================================
-- 6. CATEGORIES
-- ============================================================
CREATE TABLE categories (
                            id         BIGSERIAL PRIMARY KEY,
                            name       VARCHAR(50) UNIQUE NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            created_by VARCHAR(150),
                            updated_by VARCHAR(150)
);

-- ============================================================
-- 7. PRODUCTS
-- ============================================================
CREATE TABLE products (
                          id          BIGSERIAL PRIMARY KEY,
                          name        VARCHAR(150) NOT NULL,
                          description TEXT,
                          price       NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
                          stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
                          category_id BIGINT REFERENCES categories(id),
                          is_active   BOOLEAN   NOT NULL DEFAULT TRUE,
                          image_url   VARCHAR(500),
                          version     INTEGER   NOT NULL DEFAULT 0,
                          created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          created_by  VARCHAR(150),
                          updated_by  VARCHAR(150)
);

-- ============================================================
-- 8. ORDERS
-- ============================================================
CREATE TABLE orders (
                        id           BIGSERIAL PRIMARY KEY,
                        user_id      BIGINT NOT NULL REFERENCES users(id),
                        status       VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DONE', 'CANCELLED')),
                        total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
                        note         TEXT,
                        version      INTEGER   NOT NULL DEFAULT 0,
                        created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                        created_by   VARCHAR(150),
                        updated_by   VARCHAR(150)
);

-- ============================================================
-- 9. ORDER_ITEMS
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
-- 10. REFRESH_TOKENS
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
-- 11. PAYMENTS
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
-- 12. OTP_VERIFICATIONS
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
-- 13. INDEXES
-- ============================================================
CREATE INDEX idx_products_category         ON products(category_id);
CREATE INDEX idx_orders_user               ON orders(user_id);
CREATE INDEX idx_orders_status             ON orders(status);
CREATE INDEX idx_order_items_order         ON order_items(order_id);
CREATE INDEX idx_order_items_product       ON order_items(product_id);
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_payments_order            ON payments(order_id);
CREATE INDEX idx_payments_status           ON payments(status);
CREATE INDEX idx_otp_user_purpose          ON otp_verifications(user_id, purpose);