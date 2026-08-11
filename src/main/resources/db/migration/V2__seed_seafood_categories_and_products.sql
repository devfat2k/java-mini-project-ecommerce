-- ============================================================
--   roles, permissions, role_permissions
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