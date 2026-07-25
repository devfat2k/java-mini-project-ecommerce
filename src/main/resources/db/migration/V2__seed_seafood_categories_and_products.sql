INSERT INTO categories (name, created_at) VALUES
                                              ('Cá biển', now()),
                                              ('Tôm', now()),
                                              ('Mực & Bạch tuộc', now()),
                                              ('Cua & Ghẹ', now()),
                                              ('Ốc & Nghêu Sò', now()),
                                              ('Set hải sản văn phòng', now()),
                                              ('Set hải sản nhậu', now()),
                                              ('Hải sản khô', now()),
                                              ('Nước mắm & Gia vị', now());

INSERT INTO products (name, description, price, stock, category_id, is_active, created_at, updated_at, version) VALUES
-- Cá biển
('Cá thu tươi 1kg', 'Cá thu biển tươi, làm sạch sẵn, đóng gói 1kg/túi', 220000, 40, (SELECT id FROM categories WHERE name = 'Cá biển'), true, now(), now(), 0),
('Cá bớp phile 1kg', 'Phile cá bớp không xương, thịt chắc ngọt', 320000, 30, (SELECT id FROM categories WHERE name = 'Cá biển'), true, now(), now(), 0),
('Cá hồi Nauy phile 500g', 'Cá hồi nhập khẩu Nauy, cắt phile tươi', 280000, 25, (SELECT id FROM categories WHERE name = 'Cá biển'), true, now(), now(), 0),
('Cá chẽm tươi nguyên con 1kg', 'Cá chẽm biển, làm sạch vảy ruột', 180000, 35, (SELECT id FROM categories WHERE name = 'Cá biển'), true, now(), now(), 0),

-- Tôm
('Tôm sú tươi size 20-30 con/kg', 'Tôm sú biển size trung, còn sống khi thu hoạch', 320000, 50, (SELECT id FROM categories WHERE name = 'Tôm'), true, now(), now(), 0),
('Tôm thẻ chân trắng size 30-40 con/kg', 'Tôm thẻ tươi, thịt ngọt, giá phổ thông', 180000, 80, (SELECT id FROM categories WHERE name = 'Tôm'), true, now(), now(), 0),
('Tôm càng xanh size 10-12 con/kg', 'Tôm càng xanh loại lớn, thịt chắc', 450000, 25, (SELECT id FROM categories WHERE name = 'Tôm'), true, now(), now(), 0),
('Tôm hùm baby Alaska 500g', 'Tôm hùm nhập khẩu size nhỏ, phù hợp 1-2 người ăn', 890000, 15, (SELECT id FROM categories WHERE name = 'Tôm'), true, now(), now(), 0),

-- Mực & Bạch tuộc
('Mực ống tươi loại 1 - 1kg', 'Mực ống size lớn, thân dày, tươi trong ngày', 260000, 45, (SELECT id FROM categories WHERE name = 'Mực & Bạch tuộc'), true, now(), now(), 0),
('Mực lá tươi 1kg', 'Mực lá thân dẹp, thịt giòn ngọt', 320000, 30, (SELECT id FROM categories WHERE name = 'Mực & Bạch tuộc'), true, now(), now(), 0),
('Bạch tuộc tươi 1kg', 'Bạch tuộc biển làm sạch sẵn', 250000, 35, (SELECT id FROM categories WHERE name = 'Mực & Bạch tuộc'), true, now(), now(), 0),
('Mực trứng tươi 500g', 'Mực trứng non, phù hợp hấp/nướng', 220000, 20, (SELECT id FROM categories WHERE name = 'Mực & Bạch tuộc'), true, now(), now(), 0),

-- Cua & Ghẹ
('Cua biển gạch son 1kg (2-3 con)', 'Cua gạch chắc thịt, size lớn', 480000, 20, (SELECT id FROM categories WHERE name = 'Cua & Ghẹ'), true, now(), now(), 0),
('Ghẹ xanh Phú Quốc 1kg', 'Ghẹ xanh tự nhiên, thịt ngọt chắc', 380000, 30, (SELECT id FROM categories WHERE name = 'Cua & Ghẹ'), true, now(), now(), 0),
('Cua thịt Cà Mau 1kg', 'Cua thịt chắc, ít gạch, giá mềm hơn cua gạch', 420000, 25, (SELECT id FROM categories WHERE name = 'Cua & Ghẹ'), true, now(), now(), 0),
('Ghẹ ba chấm size lớn 1kg', 'Ghẹ ba chấm biển, size đồng đều', 350000, 28, (SELECT id FROM categories WHERE name = 'Cua & Ghẹ'), true, now(), now(), 0),

-- Ốc & Nghêu Sò
('Ốc hương tươi 1kg', 'Ốc hương biển, thịt giòn thơm', 320000, 30, (SELECT id FROM categories WHERE name = 'Ốc & Nghêu Sò'), true, now(), now(), 0),
('Nghêu trắng 1kg', 'Nghêu tươi size trung, giá bình dân', 65000, 100, (SELECT id FROM categories WHERE name = 'Ốc & Nghêu Sò'), true, now(), now(), 0),
('Sò huyết 1kg', 'Sò huyết đầm phá, thịt đỏ hồng đặc trưng', 180000, 50, (SELECT id FROM categories WHERE name = 'Ốc & Nghêu Sò'), true, now(), now(), 0),
('Sò điệp Nhật 500g', 'Sò điệp nhập khẩu, cồi lớn', 250000, 20, (SELECT id FROM categories WHERE name = 'Ốc & Nghêu Sò'), true, now(), now(), 0),

-- Set hải sản văn phòng
('Set hải sản 1 người ăn', 'Tôm + cá phile + mực, chế biến sẵn, hâm nóng là dùng được', 149000, 60, (SELECT id FROM categories WHERE name = 'Set hải sản văn phòng'), true, now(), now(), 0),
('Set cơm văn phòng hải sản hấp sẵn', 'Hải sản hấp kèm cơm, tiện mang đi làm', 129000, 70, (SELECT id FROM categories WHERE name = 'Set hải sản văn phòng'), true, now(), now(), 0),
('Set salad hải sản eat-clean', 'Tôm, mực trụng kèm rau củ, ít calo', 139000, 50, (SELECT id FROM categories WHERE name = 'Set hải sản văn phòng'), true, now(), now(), 0),
('Set lẩu hải sản mini 1-2 người', 'Lẩu hải sản đóng gói sẵn nguyên liệu, chỉ cần nấu', 259000, 40, (SELECT id FROM categories WHERE name = 'Set hải sản văn phòng'), true, now(), now(), 0),

-- Set hải sản nhậu
('Set nhậu hải sản 4-6 người', 'Tôm, mực, nghêu, cá — đủ món nhậu cho nhóm bạn', 890000, 20, (SELECT id FROM categories WHERE name = 'Set hải sản nhậu'), true, now(), now(), 0),
('Set nướng hải sản BBQ', 'Tôm, mực, bạch tuộc, sò điệp kèm sốt nướng', 750000, 25, (SELECT id FROM categories WHERE name = 'Set hải sản nhậu'), true, now(), now(), 0),
('Set hải sản hấp bia', 'Nghêu, sò huyết, tôm, ốc hấp sả bia', 650000, 25, (SELECT id FROM categories WHERE name = 'Set hải sản nhậu'), true, now(), now(), 0),
('Combo hải sản 8 món tiệc cuối tuần', 'Set lớn 8 loại hải sản, phù hợp tiệc gia đình/công ty', 1590000, 10, (SELECT id FROM categories WHERE name = 'Set hải sản nhậu'), true, now(), now(), 0),

-- Hải sản khô
('Tôm khô loại 1 500g', 'Tôm khô size lớn, màu đỏ tự nhiên', 350000, 30, (SELECT id FROM categories WHERE name = 'Hải sản khô'), true, now(), now(), 0),
('Cá cơm khô 500g', 'Cá cơm phơi nắng, không chất bảo quản', 120000, 50, (SELECT id FROM categories WHERE name = 'Hải sản khô'), true, now(), now(), 0),
('Mực khô loại 1 - 1 con', 'Mực khô nguyên con size lớn', 280000, 25, (SELECT id FROM categories WHERE name = 'Hải sản khô'), true, now(), now(), 0),
('Cá thu một nắng 500g', 'Cá thu phơi một nắng, giữ độ ngọt tự nhiên', 190000, 30, (SELECT id FROM categories WHERE name = 'Hải sản khô'), true, now(), now(), 0),

-- Nước mắm & Gia vị
('Nước mắm nhĩ Phú Quốc 40 độ đạm 500ml', 'Nước mắm truyền thống, độ đạm cao', 185000, 60, (SELECT id FROM categories WHERE name = 'Nước mắm & Gia vị'), true, now(), now(), 0),
('Nước mắm cá cơm Phan Thiết 500ml', 'Nước mắm cá cơm nguyên chất', 95000, 80, (SELECT id FROM categories WHERE name = 'Nước mắm & Gia vị'), true, now(), now(), 0),
('Muối tiêu chanh chấm hải sản 100g', 'Gia vị chấm chuyên dụng cho hải sản hấp', 25000, 100, (SELECT id FROM categories WHERE name = 'Nước mắm & Gia vị'), true, now(), now(), 0),
('Sốt me chấm hải sản 250g', 'Sốt me chua ngọt, hợp mực/tôm nướng', 45000, 70, (SELECT id FROM categories WHERE name = 'Nước mắm & Gia vị'), true, now(), now(), 0);