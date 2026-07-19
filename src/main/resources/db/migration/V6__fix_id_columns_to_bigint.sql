-- Users
ALTER TABLE users ALTER COLUMN id TYPE BIGINT;

-- Categories
ALTER TABLE categories ALTER COLUMN id TYPE BIGINT;

-- Products
ALTER TABLE products ALTER COLUMN id TYPE BIGINT;
ALTER TABLE products ALTER COLUMN category_id TYPE BIGINT;

-- Orders
ALTER TABLE orders ALTER COLUMN id TYPE BIGINT;
ALTER TABLE orders ALTER COLUMN user_id TYPE BIGINT;

-- Order items
ALTER TABLE order_items ALTER COLUMN id TYPE BIGINT;
ALTER TABLE order_items ALTER COLUMN order_id TYPE BIGINT;
ALTER TABLE order_items ALTER COLUMN product_id TYPE BIGINT;

-- Refresh tokens (user_id đã đúng BIGINT sẵn, chỉ cần sửa id)
ALTER TABLE refresh_tokens ALTER COLUMN id TYPE BIGINT;

-- Payments (id đã đúng BIGSERIAL sẵn, chỉ cần đảm bảo order_id đúng)
ALTER TABLE payments ALTER COLUMN order_id TYPE BIGINT;