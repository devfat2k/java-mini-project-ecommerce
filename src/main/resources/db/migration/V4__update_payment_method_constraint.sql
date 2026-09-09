-- Xóa check constraint cũ
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_method_check;
-- Thêm check constraint mới đồng bộ với enum PaymentMethod
ALTER TABLE payments ADD CONSTRAINT payments_payment_method_check
    CHECK (payment_method IN ('VNPAY', 'COD', 'MOMO', 'ZALOPAY', 'BANK', 'WALLET', 'CASH'));
