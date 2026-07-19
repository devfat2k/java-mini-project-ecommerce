-- Sửa lại kiểu dữ liệu order_id cho khớp OrderEntity.id (Long → BIGINT)
ALTER TABLE payments
ALTER COLUMN order_id TYPE BIGINT;