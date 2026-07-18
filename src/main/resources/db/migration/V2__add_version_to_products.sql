-- Thêm cột version để phục vụ Optimistic Lock (@Version trong Hibernate)
ALTER TABLE products
ADD COLUMN version INTEGER NOT NULL DEFAULT 0;