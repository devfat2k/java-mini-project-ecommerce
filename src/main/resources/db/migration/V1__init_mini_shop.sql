-- ============================================================
-- MINI SHOP — Schema v2 (sạch, chuẩn production)
-- Dùng cho: luyện tập PostgreSQL + Spring Boot project
-- Tạo: 30/06/2026
-- ============================================================

-- ============================================================
-- 0. RESET (chạy khi cần bắt đầu lại từ đầu)
-- ============================================================
-- DROP TABLE IF EXISTS order_items  CASCADE;
-- DROP TABLE IF EXISTS orders       CASCADE;
-- DROP TABLE IF EXISTS products     CASCADE;
-- DROP TABLE IF EXISTS categories   CASCADE;
-- DROP TABLE IF EXISTS users        CASCADE;

CREATE SCHEMA IF NOT EXISTS public;

SET search_path TO public;
-- ============================================================
-- 1. USERS
-- Sửa: thêm updated_at, đặt tên nhất quán created_at
-- ============================================================
CREATE TABLE users (
                       id           SERIAL PRIMARY KEY,
                       full_name    VARCHAR(100) NOT NULL,
                       email        VARCHAR(150) UNIQUE NOT NULL,
                       phone_number VARCHAR(15)  UNIQUE NOT NULL,
                       password     VARCHAR(255) NOT NULL,
                       role         VARCHAR(20)  NOT NULL DEFAULT 'USER'
                           CHECK (role IN ('USER', 'ADMIN')),
                       is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 2. CATEGORIES
-- ============================================================
CREATE TABLE categories (
                            id         SERIAL PRIMARY KEY,
                            name       VARCHAR(50) UNIQUE NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 3. PRODUCTS
-- Sửa: thêm updated_at, thêm description
-- ============================================================
CREATE TABLE products (
                          id          SERIAL PRIMARY KEY,
                          name        VARCHAR(150) NOT NULL,
                          description TEXT,
                          price       NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
                          stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
                          category_id INTEGER REFERENCES categories(id),
                          is_active   BOOLEAN   NOT NULL DEFAULT TRUE,
                          created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 4. ORDERS
-- Sửa: thêm updated_at
-- ============================================================
CREATE TABLE orders (
                        id           SERIAL PRIMARY KEY,
                        user_id      INTEGER NOT NULL REFERENCES users(id),
                        status       VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DONE', 'CANCELLED')),
                        total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
                        note         TEXT,
                        created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 5. ORDER_ITEMS
-- Bảng trung gian N-N: orders <-> products
-- ============================================================
CREATE TABLE order_items (
                             id         SERIAL PRIMARY KEY,
                             order_id   INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id INTEGER NOT NULL REFERENCES products(id),
                             quantity   INTEGER        NOT NULL CHECK (quantity > 0),
                             unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0)
);


-- ============================================================
-- INDEX: tăng tốc các query JOIN hay dùng
-- Fresher cần hiểu tại sao có những dòng này
-- ============================================================
CREATE INDEX idx_products_category    ON products(category_id);
CREATE INDEX idx_orders_user          ON orders(user_id);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_order_items_order    ON order_items(order_id);
CREATE INDEX idx_order_items_product  ON order_items(product_id);


-- ============================================================
-- DATA: USERS (30 users + 1 admin)
-- ============================================================
-- INSERT INTO users (full_name, email, phone_number, role) VALUES
--                                                              ('Nguyễn Văn Bảo',    'bao.nguyen@example.com',    '0901000001', 'USER'),
--                                                              ('Trần Quốc Bình',    'binh.tran@example.com',     '0901000002', 'USER'),
--                                                              ('Lê Minh Châu',      'chau.le@example.com',       '0901000003', 'USER'),
--                                                              ('Phạm Gia Đạt',      'dat.pham@example.com',      '0901000004', 'USER'),
--                                                              ('Hoàng Đức Huy',     'huy.hoang@example.com',     '0901000005', 'USER'),
--                                                              ('Võ Thanh Khoa',     'khoa.vo@example.com',       '0901000006', 'USER'),
--                                                              ('Đoàn Minh Long',    'long.doan@example.com',     '0901000007', 'USER'),
--                                                              ('Bùi Quốc Minh',     'minh.bui@example.com',      '0901000008', 'USER'),
--                                                              ('Đặng Thanh Nam',    'nam.dang@example.com',      '0901000009', 'USER'),
--                                                              ('Phan Ngọc Phúc',    'phuc.phan@example.com',     '0901000010', 'USER'),
--                                                              ('Ngô Thanh Quân',    'quan.ngo@example.com',      '0901000011', 'USER'),
--                                                              ('Lý Minh Sơn',       'son.ly@example.com',        '0901000012', 'USER'),
--                                                              ('Huỳnh Quang Tài',   'tai.huynh@example.com',     '0901000013', 'USER'),
--                                                              ('Mai Anh Tuấn',      'tuan.mai@example.com',      '0901000014', 'USER'),
--                                                              ('Vũ Đức Việt',       'viet.vu@example.com',       '0901000015', 'USER'),
--                                                              ('Cao Minh Anh',      'anh.cao@example.com',       '0901000016', 'USER'),
--                                                              ('Nguyễn Hải Đăng',   'dang.nguyen@example.com',   '0901000017', 'USER'),
--                                                              ('Trần Minh Đức',     'duc.tran@example.com',      '0901000018', 'USER'),
--                                                              ('Lê Quang Hiếu',     'hieu.le@example.com',       '0901000019', 'USER'),
--                                                              ('Phạm Thanh Kiệt',   'kiet.pham@example.com',     '0901000020', 'USER'),
--                                                              ('Hoàng Gia Khánh',   'khanh.hoang@example.com',   '0901000021', 'USER'),
--                                                              ('Võ Minh Lâm',       'lam.vo@example.com',        '0901000022', 'USER'),
--                                                              ('Đỗ Thanh Nhân',     'nhan.do@example.com',       '0901000023', 'USER'),
--                                                              ('Bùi Tuấn Phong',    'phong.bui@example.com',     '0901000024', 'USER'),
--                                                              ('Đặng Quốc Thịnh',   'thinh.dang@example.com',    '0901000025', 'USER'),
--                                                              ('Phan Minh Trí',     'tri.phan@example.com',      '0901000026', 'USER'),
--                                                              ('Ngô Hoàng Vũ',      'vu.ngo@example.com',        '0901000027', 'USER'),
--                                                              ('Lý Gia Bảo',        'giabao.ly@example.com',     '0901000028', 'USER'),
--                                                              ('Huỳnh Thanh Tâm',   'tam.huynh@example.com',     '0901000029', 'USER'),
--                                                              ('Nguyễn Hữu Phát',   'phathn2688@gmail.com',      '0901000030', 'ADMIN');


-- ============================================================
-- DATA: CATEGORIES
-- ============================================================
INSERT INTO categories (name) VALUES
                                  ('Điện thoại'), ('Laptop'), ('Máy tính bảng'), ('Đồng hồ thông minh'),
                                  ('Tai nghe'), ('Loa Bluetooth'), ('Bàn phím'), ('Chuột'),
                                  ('Màn hình'), ('PC Gaming'), ('Linh kiện PC'), ('Ổ cứng SSD'),
                                  ('RAM'), ('Thiết bị mạng'), ('Máy in'), ('Camera'),
                                  ('Pin sạc dự phòng'), ('Cáp sạc'), ('Sạc điện thoại'), ('Phụ kiện khác');


-- ============================================================
-- DATA: PRODUCTS (30 sản phẩm)
-- ============================================================
INSERT INTO products (name, price, stock, category_id) VALUES
                                                           ('iPhone 16 128GB',                22990000, 20, 1),
                                                           ('iPhone 16 Pro 256GB',            30990000, 15, 1),
                                                           ('Samsung Galaxy S25 Ultra 256GB', 28990000, 18, 1),
                                                           ('Xiaomi 15 256GB',                16990000, 25, 1),
                                                           ('OPPO Reno14 5G',                  9990000, 30, 1),
                                                           ('MacBook Air M4 13"',             26990000, 12, 2),
                                                           ('MacBook Pro M4 14"',             42990000,  8, 2),
                                                           ('Dell XPS 14',                    38990000,  6, 2),
                                                           ('ASUS Vivobook S14 OLED',         21990000, 15, 2),
                                                           ('Lenovo ThinkPad E14 Gen 6',      24990000, 10, 2),
                                                           ('iPad Air M3 11"',                18990000, 18, 3),
                                                           ('Samsung Galaxy Tab S10 FE',      13990000, 20, 3),
                                                           ('Apple Watch Series 10 GPS 46mm', 11990000, 16, 4),
                                                           ('Samsung Galaxy Watch Ultra',     12990000, 10, 4),
                                                           ('AirPods Pro 2 USB-C',             5890000, 35, 5),
                                                           ('Sony WH-1000XM6',                 9990000, 12, 5),
                                                           ('Galaxy Buds3 Pro',                4490000, 22, 5),
                                                           ('JBL Flip 7',                      3290000, 18, 6),
                                                           ('Marshall Emberton III',           4490000, 10, 6),
                                                           ('Keychron K8 Pro',                 2590000, 25, 7),
                                                           ('Logitech MX Keys S',              2790000, 14, 7),
                                                           ('Logitech MX Master 3S',           2290000, 20, 8),
                                                           ('Logitech G304 Lightspeed',         890000, 30, 8),
                                                           ('LG UltraGear 27GS60F-B 27"',      4890000, 10, 9),
                                                           ('Dell UltraSharp U2724D',          8990000,  8, 9),
                                                           ('Samsung 990 Pro 1TB',             2890000, 24, 12),
                                                           ('Kingston NV3 1TB',                1790000, 35, 12),
                                                           ('Kingston Fury Beast DDR5 32GB',   3190000, 20, 13),
                                                           ('TP-Link Archer AX55',             2290000, 15, 14),
                                                           ('Anker Prime 100W USB-C Charger',  1790000, 30, 19);


-- ============================================================
-- DATA: ORDERS (23 đơn, đa dạng status và thời gian)
-- ============================================================
-- INSERT INTO orders (user_id, status, total_amount, created_at) VALUES
--                                                                    (1,  'DONE',      38670000, NOW() - INTERVAL '45 days'),
--                                                                    (1,  'SHIPPED',    5890000, NOW() - INTERVAL '8 days'),
--                                                                    (2,  'PENDING',   16990000, NOW() - INTERVAL '2 days'),
--                                                                    (3,  'DONE',      31980000, NOW() - INTERVAL '25 days'),
--                                                                    (3,  'CANCELLED',  9990000, NOW() - INTERVAL '18 days'),
--                                                                    (4,  'DONE',       4890000, NOW() - INTERVAL '12 days'),
--                                                                    (5,  'CONFIRMED', 24880000, NOW() - INTERVAL '3 days'),
--                                                                    (6,  'DONE',      18990000, NOW() - INTERVAL '28 days'),
--                                                                    (7,  'SHIPPED',    4490000, NOW() - INTERVAL '5 days'),
--                                                                    (8,  'DONE',      49260000, NOW() - INTERVAL '35 days'),
--                                                                    (8,  'PENDING',    1790000, NOW() - INTERVAL '6 hours'),
--                                                                    (9,  'DONE',       5680000, NOW() - INTERVAL '15 days'),
--                                                                    (10, 'CONFIRMED', 17880000, NOW() - INTERVAL '1 day'),
--                                                                    (11, 'DONE',      45880000, NOW() - INTERVAL '40 days'),
--                                                                    (12, 'DONE',        890000, NOW() - INTERVAL '22 days'),
--                                                                    (13, 'SHIPPED',   14980000, NOW() - INTERVAL '7 days'),
--                                                                    (14, 'DONE',      38790000, NOW() - INTERVAL '50 days'),
--                                                                    (15, 'PENDING',    2790000, NOW() - INTERVAL '10 hours'),
--                                                                    (16, 'DONE',      10280000, NOW() - INTERVAL '16 days'),
--                                                                    (17, 'DONE',       3190000, NOW() - INTERVAL '14 days'),
--                                                                    (18, 'CONFIRMED', 24780000, NOW() - INTERVAL '4 days'),
--                                                                    (19, 'DONE',       2290000, NOW() - INTERVAL '11 days'),
--                                                                    (20, 'SHIPPED',   12990000, NOW() - INTERVAL '9 days');


-- ============================================================
-- DATA: ORDER_ITEMS (quantity đa dạng từ 1-4)
-- ============================================================
-- INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
--                                                                          (1,  2,  1, 30990000),
--                                                                          (1,  15, 2,  5890000),
--                                                                          (1,  30, 3,  1790000),
--                                                                          (2,  15, 1,  5890000),
--                                                                          (3,  4,  1, 16990000),
--                                                                          (4,  6,  1, 26990000),
--                                                                          (4,  23, 2,  2290000),
--                                                                          (4,  21, 1,  2790000),
--                                                                          (5,  5,  1,  9990000),
--                                                                          (6,  24, 1,  4890000),
--                                                                          (7,  11, 1, 18990000),
--                                                                          (7,  15, 1,  5890000),
--                                                                          (8,  11, 1, 18990000),
--                                                                          (9,  19, 1,  4490000),
--                                                                          (10, 7,  1, 42990000),
--                                                                          (10, 26, 2,  2890000),
--                                                                          (10, 30, 1,  1790000),
--                                                                          (11, 30, 1,  1790000),
--                                                                          (12, 19, 1,  4490000),
--                                                                          (12, 29, 4,   297500),
--                                                                          (13, 14, 1, 12990000),
--                                                                          (13, 30, 1,  1790000),
--                                                                          (13, 29, 2,  1550000),
--                                                                          (14, 8,  1, 38990000),
--                                                                          (14, 23, 1,  2290000),
--                                                                          (14, 21, 1,  2790000),
--                                                                          (14, 30, 1,  1810000),
--                                                                          (15, 22, 1,   890000),
--                                                                          (16, 13, 1, 11990000),
--                                                                          (16, 30, 1,  1790000),
--                                                                          (16, 29, 3,   400000),
--                                                                          (17, 8,  1, 38990000),
--                                                                          (17, 29, 2,   400000),
--                                                                          (18, 21, 1,  2790000),
--                                                                          (19, 16, 1,  9990000),
--                                                                          (19, 29, 1,   290000),
--                                                                          (20, 28, 1,  3190000),
--                                                                          (21, 1,  1, 22990000),
--                                                                          (21, 30, 1,  1790000),
--                                                                          (22, 23, 1,  2290000),
--                                                                          (23, 9,  2, 21990000);
--

-- ============================================================
-- KIỂM TRA NHANH SAU KHI CHẠY
-- Bỏ comment từng câu để xác nhận data đã đúng
-- ============================================================
-- SELECT COUNT(*) FROM users;       -- kỳ vọng: 30
-- SELECT COUNT(*) FROM categories;  -- kỳ vọng: 20
-- SELECT COUNT(*) FROM products;    -- kỳ vọng: 30
-- SELECT COUNT(*) FROM orders;      -- kỳ vọng: 23
-- SELECT COUNT(*) FROM order_items; -- kỳ vọng: 41