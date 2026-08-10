-- ============================================================
-- MINI SHOP — Seed Data V2
-- Chỉ insert dữ liệu cho các bảng KHÔNG phụ thuộc users/orders/payments:
--   roles, permissions, role_permissions, categories, products
-- Không đụng tới V1, không tạo user/order/payment.
-- ============================================================

-- ============================================================
-- 1. ROLES
-- ============================================================
INSERT INTO roles (name, description, created_by, updated_by) VALUES
                                                                  ('ADMIN',    'Quản trị viên hệ thống, toàn quyền',      'system', 'system'),
                                                                  ('STAFF',    'Nhân viên bán hàng / vận hành',            'system', 'system'),
                                                                  ('CUSTOMER', 'Khách hàng mua sắm',                       'system', 'system');

-- ============================================================
-- 2. PERMISSIONS
-- ============================================================
INSERT INTO permissions (code, description, created_by, updated_by) VALUES
                                                                        ('PRODUCT_CREATE',        'Tạo sản phẩm mới',                'system', 'system'),
                                                                        ('PRODUCT_UPDATE',        'Cập nhật sản phẩm',               'system', 'system'),
                                                                        ('PRODUCT_DELETE',        'Xóa sản phẩm',                    'system', 'system'),
                                                                        ('PRODUCT_VIEW',          'Xem sản phẩm',                    'system', 'system'),
                                                                        ('CATEGORY_CREATE',       'Tạo danh mục',                    'system', 'system'),
                                                                        ('CATEGORY_UPDATE',       'Cập nhật danh mục',               'system', 'system'),
                                                                        ('CATEGORY_DELETE',       'Xóa danh mục',                    'system', 'system'),
                                                                        ('ORDER_VIEW',            'Xem đơn hàng',                    'system', 'system'),
                                                                        ('ORDER_VIEW_ALL',        'Xem tất cả đơn hàng (admin)',     'system', 'system'),
                                                                        ('ORDER_UPDATE_STATUS',   'Cập nhật trạng thái đơn hàng',    'system', 'system'),
                                                                        ('ORDER_CANCEL',          'Hủy đơn hàng',                    'system', 'system'),
                                                                        ('USER_MANAGE',           'Quản lý người dùng',              'system', 'system'),
                                                                        ('ROLE_MANAGE',           'Quản lý vai trò & quyền',         'system', 'system'),
                                                                        ('PAYMENT_VIEW',          'Xem thông tin thanh toán',        'system', 'system');

-- ============================================================
-- 3. ROLE_PERMISSIONS
--    ADMIN  -> tất cả quyền
--    STAFF  -> quản lý sản phẩm/danh mục + xử lý đơn hàng
--    CUSTOMER -> chỉ xem sản phẩm + xem/hủy đơn của chính mình
-- ============================================================
INSERT INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, 'system'
FROM roles r
         JOIN permissions p ON TRUE
WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, 'system'
FROM roles r
         JOIN permissions p ON p.code IN (
                                          'PRODUCT_CREATE', 'PRODUCT_UPDATE', 'PRODUCT_VIEW',
                                          'CATEGORY_CREATE', 'CATEGORY_UPDATE',
                                          'ORDER_VIEW_ALL', 'ORDER_UPDATE_STATUS',
                                          'PAYMENT_VIEW'
    )
WHERE r.name = 'STAFF';

INSERT INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, 'system'
FROM roles r
         JOIN permissions p ON p.code IN (
                                          'PRODUCT_VIEW', 'ORDER_VIEW', 'ORDER_CANCEL'
    )
WHERE r.name = 'CUSTOMER';

-- ============================================================
-- 4. CATEGORIES (Hải sản Phan Thiết)
-- ============================================================
INSERT INTO categories (name, created_by, updated_by) VALUES
                                                          ('Tôm',              'system', 'system'),
                                                          ('Cá',               'system', 'system'),
                                                          ('Mực',              'system', 'system'),
                                                          ('Cua - Ghẹ',        'system', 'system'),
                                                          ('Ốc - Sò - Nghêu',  'system', 'system'),
                                                          ('Hải sản khô',      'system', 'system'),
                                                          ('Nước mắm - Gia vị','system', 'system');

-- ============================================================
-- 5. PRODUCTS
-- ============================================================
INSERT INTO products (name, description, price, stock, category_id, is_active, image_url, unit, tags, created_by, updated_by)
SELECT 'Tôm sú tươi size 20-30', 'Tôm sú Phan Thiết đánh bắt trong ngày, size 20-30 con/kg', 285000, 50, c.id, TRUE, NULL, 'kg', ARRAY['tuoi','ship-nhanh'], 'system', 'system'
FROM categories c WHERE c.name = 'Tôm'
UNION ALL
SELECT 'Tôm thẻ đông lạnh', 'Tôm thẻ cấp đông theo công nghệ IQF, giữ trọn độ tươi ngon', 165000, 80, c.id, TRUE, NULL, 'kg', ARRAY['dong-lanh'], 'system', 'system'
FROM categories c WHERE c.name = 'Tôm'
UNION ALL
SELECT 'Cá thu một nắng', 'Cá thu Phan Thiết phơi một nắng chuẩn vị biển miền Trung', 220000, 40, c.id, TRUE, NULL, 'kg', ARRAY['nang-mot','dac-san'], 'system', 'system'
FROM categories c WHERE c.name = 'Cá'
UNION ALL
SELECT 'Cá bớp phi lê', 'Cá bớp tươi phi lê sẵn, thịt chắc ngọt', 195000, 35, c.id, TRUE, NULL, 'kg', ARRAY['tuoi','phi-le'], 'system', 'system'
FROM categories c WHERE c.name = 'Cá'
UNION ALL
SELECT 'Mực ống tươi', 'Mực ống câu Phan Thiết, size vừa, thịt giòn ngọt', 210000, 45, c.id, TRUE, NULL, 'kg', ARRAY['tuoi'], 'system', 'system'
FROM categories c WHERE c.name = 'Mực'
UNION ALL
SELECT 'Mực một nắng loại 1', 'Mực một nắng đặc sản Phan Thiết, nướng lên thơm lừng', 350000, 30, c.id, TRUE, NULL, 'kg', ARRAY['nang-mot','dac-san','loai-1'], 'system', 'system'
FROM categories c WHERE c.name = 'Mực'
UNION ALL
SELECT 'Ghẹ xanh Phan Thiết', 'Ghẹ xanh tươi sống, gạch đầy, size 3-4 con/kg', 320000, 25, c.id, TRUE, NULL, 'kg', ARRAY['tuoi-song'], 'system', 'system'
FROM categories c WHERE c.name = 'Cua - Ghẹ'
UNION ALL
SELECT 'Cua thịt loại đặc biệt', 'Cua thịt chắc, size lớn, phù hợp hấp hoặc rang me', 380000, 20, c.id, TRUE, NULL, 'kg', ARRAY['tuoi-song','loai-1'], 'system', 'system'
FROM categories c WHERE c.name = 'Cua - Ghẹ'
UNION ALL
SELECT 'Sò điệp tươi', 'Sò điệp Phan Thiết size lớn, ngọt thịt', 250000, 30, c.id, TRUE, NULL, 'kg', ARRAY['tuoi'], 'system', 'system'
FROM categories c WHERE c.name = 'Ốc - Sò - Nghêu'
UNION ALL
SELECT 'Ốc hương tươi sống', 'Ốc hương biển Phan Thiết, giòn thơm đặc trưng', 280000, 35, c.id, TRUE, NULL, 'kg', ARRAY['tuoi-song','dac-san'], 'system', 'system'
FROM categories c WHERE c.name = 'Ốc - Sò - Nghêu'
UNION ALL
SELECT 'Cá cơm khô loại 1', 'Cá cơm khô rim sẵn nắng, không tẩm hóa chất', 150000, 60, c.id, TRUE, NULL, 'kg', ARRAY['kho','loai-1'], 'system', 'system'
FROM categories c WHERE c.name = 'Hải sản khô'
UNION ALL
SELECT 'Tôm khô loại 1', 'Tôm khô Phan Thiết, màu đỏ tự nhiên, thịt dai ngọt', 480000, 25, c.id, TRUE, NULL, 'kg', ARRAY['kho','loai-1','dac-san'], 'system', 'system'
FROM categories c WHERE c.name = 'Hải sản khô'
UNION ALL
SELECT 'Nước mắm nhĩ Phan Thiết 40 độ đạm', 'Nước mắm truyền thống ủ chượp gỗ, 40 độ đạm nguyên chất', 120000, 100, c.id, TRUE, NULL, 'chai', ARRAY['truyen-thong','dac-san'], 'system', 'system'
FROM categories c WHERE c.name = 'Nước mắm - Gia vị'
UNION ALL
SELECT 'Muối ớt xanh Phan Thiết', 'Muối ớt xanh chấm hải sản đặc trưng miền biển', 35000, 120, c.id, TRUE, NULL, 'hũ', ARRAY['gia-vi'], 'system', 'system'
FROM categories c WHERE c.name = 'Nước mắm - Gia vị';