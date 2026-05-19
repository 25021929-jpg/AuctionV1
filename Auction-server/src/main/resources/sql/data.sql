-- ============================================================
-- DATA: users
-- 3 ADMIN, 5 SELLER, 10 BIDDER
-- password_hash: BCrypt của "Password123!"
-- ============================================================
INSERT INTO users (full_name, username, email, phone, date_of_birth, password_hash, role) VALUES
-- ADMIN
('Nguyễn Quản Trị',   'admin01',   'admin01@auction.vn',   '0901000001', '1985-03-15', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
('Trần Hệ Thống',     'admin02',   'admin02@auction.vn',   '0901000002', '1987-07-22', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
('Lê Vận Hành',       'admin03',   'admin03@auction.vn',   '0901000003', '1990-11-05', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),

-- SELLER
('Phạm Văn Bán',      'seller01',  'seller01@auction.vn',  '0902000001', '1988-04-10', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER'),
('Hoàng Thị Thu',     'seller02',  'seller02@auction.vn',  '0902000002', '1992-09-18', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER'),
('Đỗ Minh Khoa',      'seller03',  'seller03@auction.vn',  '0902000003', '1986-01-25', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER'),
('Vũ Thị Lan',        'seller04',  'seller04@auction.vn',  '0902000004', '1993-06-30', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER'),
('Bùi Quốc Hùng',    'seller05',  'seller05@auction.vn',  '0902000005', '1989-12-12', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER'),

-- BIDDER
('Nguyễn Đặt Giá',   'bidder01',  'bidder01@auction.vn',  '0903000001', '1995-02-14', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Trần Mua Hàng',     'bidder02',  'bidder02@auction.vn',  '0903000002', '1997-08-07', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Lê Thị Hoa',        'bidder03',  'bidder03@auction.vn',  '0903000003', '1994-05-21', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Phạm Đức Nam',      'bidder04',  'bidder04@auction.vn',  '0903000004', '1996-11-03', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Hoàng Văn Long',    'bidder05',  'bidder05@auction.vn',  '0903000005', '1991-07-16', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Đinh Thị Mai',      'bidder06',  'bidder06@auction.vn',  '0903000006', '1998-03-29', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Ngô Minh Tuấn',     'bidder07',  'bidder07@auction.vn',  '0903000007', '1993-10-08', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Cao Thị Nhung',     'bidder08',  'bidder08@auction.vn',  '0903000008', '1999-01-17', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Dương Văn Tùng',   'bidder09',  'bidder09@auction.vn',  '0903000009', '1990-06-11', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER'),
('Lý Thị Bảo',        'bidder10',  'bidder10@auction.vn',  '0903000010', '1996-09-23', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BIDDER');

-- ============================================================
-- DATA: categories
-- ============================================================
INSERT INTO categories (category_name, description) VALUES
                                                        ('Đồng hồ cao cấp',     'Đồng hồ thương hiệu nổi tiếng: Rolex, Omega, Patek Philippe...'),
                                                        ('Tranh nghệ thuật',     'Tranh sơn dầu, tranh lụa, tác phẩm nghệ thuật có giá trị'),
                                                        ('Trang sức & Kim cương','Nhẫn, vòng cổ, bông tai từ vàng 18K, 24K và kim cương'),
                                                        ('Bất động sản',         'Đất nền, căn hộ, biệt thự đưa ra đấu giá'),
                                                        ('Ô tô xe máy',          'Xe hơi cổ điển, xe máy phân khối lớn, xe độ chế'),
                                                        ('Điện tử công nghệ',    'Laptop, điện thoại, thiết bị điện tử phiên bản giới hạn'),
                                                        ('Rượu vang & Whisky',   'Rượu vang vintage, whisky đơn mạch lâu năm'),
                                                        ('Cổ vật & Đồ cổ',      'Gốm sứ, đồ đồng, vật phẩm thời nhà Nguyễn, nhà Lý...');

-- ============================================================
-- DATA: auction_items
-- Bao gồm đủ 4 trạng thái: ACTIVE, PENDING, CLOSED, CANCELLED
-- seller_id: 4-8 (seller01 -> seller05)
-- category_id: 1-8
-- ============================================================
INSERT INTO auction_items (seller_id, category_id, item_name, description, starting_price, current_price, min_bid_increment, status, start_time, end_time) VALUES

-- ACTIVE (đang diễn ra)
(4, 1, 'Đồng hồ Rolex Submariner 2020',
 'Rolex Submariner Date 116610LN, full box & papers, tình trạng 99%',
 150000000, 162000000, 1000000, 'ACTIVE',
 '2025-07-01 08:00:00', '2025-07-15 20:00:00'),

(5, 3, 'Nhẫn kim cương 1.5 carat GIA',
 'Kim cương thiên nhiên 1.5ct, chứng nhận GIA, màu G, độ tinh khiết VS1, vàng trắng 18K',
 80000000, 95000000, 500000, 'ACTIVE',
 '2025-07-03 09:00:00', '2025-07-20 18:00:00'),

(6, 2, 'Tranh sơn dầu "Phố Hà Nội" - Bùi Xuân Phái',
 'Tác phẩm gốc của họa sĩ Bùi Xuân Phái, sơn dầu trên vải, kích thước 60x80cm, có chứng thực',
 200000000, 215000000, 2000000, 'ACTIVE',
 '2025-07-05 10:00:00', '2025-07-25 17:00:00'),

(7, 5, 'Toyota Land Cruiser 1980 Phục Chế',
 'Land Cruiser FJ40 đời 1980, đã qua phục chế toàn bộ, máy mới, nội thất da thật',
 450000000, 468000000, 5000000, 'ACTIVE',
 '2025-07-02 08:00:00', '2025-07-18 20:00:00'),

(8, 7, 'Rượu Macallan 25 năm Single Malt',
 'Macallan 25 Years Old Sherry Oak, chai 700ml, nguyên seal, vintage 1995',
 25000000, 28000000, 500000, 'ACTIVE',
 '2025-07-04 12:00:00', '2025-07-14 22:00:00'),

-- PENDING (sắp diễn ra)
(4, 8, 'Bình gốm Lý triều thế kỷ XII',
 'Bình hoa gốm men ngọc thời Lý, kèm giấy chứng nhận bảo tàng, hiện vật quý hiếm',
 500000000, 500000000, 10000000, 'PENDING',
 '2025-07-20 09:00:00', '2025-08-05 17:00:00'),

(5, 6, 'iPhone 16 Pro Max Space Black 1TB - Bản Prototype',
 'Bản prototype chưa ra thị trường, màu Space Black giới hạn, cực hiếm',
 35000000, 35000000, 500000, 'PENDING',
 '2025-07-25 08:00:00', '2025-08-10 20:00:00'),

(6, 4, 'Lô đất nền Quận 9 - 120m2',
 'Đất thổ cư 120m2, đường 12m, sổ hồng riêng, khu dân cư hiện hữu Quận 9 TP.HCM',
 3500000000, 3500000000, 50000000, 'PENDING',
 '2025-07-22 10:00:00', '2025-08-15 17:00:00'),

-- CLOSED (đã kết thúc - có transaction)
(7, 1, 'Đồng hồ Omega Speedmaster Moon Watch 1969',
 'Omega Speedmaster Professional phiên bản Apollo 11, full set, tình trạng hoàn hảo',
 120000000, 148000000, 1000000, 'CLOSED',
 '2025-06-01 08:00:00', '2025-06-15 20:00:00'),

(8, 3, 'Vòng cổ ngọc trai Akoya 8mm x 45 hạt',
 'Ngọc trai Akoya Nhật Bản, đường kính 8mm, 45 hạt, khóa vàng 18K nạm kim cương',
 45000000, 57000000, 500000, 'CLOSED',
 '2025-06-05 09:00:00', '2025-06-20 18:00:00'),

-- CANCELLED
(4, 2, 'Tranh lụa "Thiếu nữ bên hoa huệ" - Tô Ngọc Vân',
 'Phiên đấu giá bị huỷ do tranh chấp nguồn gốc tác phẩm',
 300000000, 300000000, 5000000, 'CANCELLED',
 '2025-05-10 08:00:00', '2025-05-25 20:00:00');

-- ============================================================
-- DATA: bids
-- Lịch sử đặt giá cho các phiên ACTIVE và CLOSED
-- bidder_id: 9-18 (bidder01 -> bidder10)
-- ============================================================

-- Bids cho item 1 (Rolex Submariner - ACTIVE)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (1,  9, 151000000, '2025-07-01 09:15:00'),
                                                                (1, 10, 153000000, '2025-07-01 11:30:00'),
                                                                (1, 11, 155000000, '2025-07-02 08:45:00'),
                                                                (1,  9, 157000000, '2025-07-02 14:00:00'),
                                                                (1, 12, 159000000, '2025-07-03 10:20:00'),
                                                                (1, 10, 162000000, '2025-07-04 16:35:00');

-- Bids cho item 2 (Nhẫn kim cương - ACTIVE)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (2, 13, 81000000, '2025-07-03 10:00:00'),
                                                                (2, 14, 83000000, '2025-07-04 12:15:00'),
                                                                (2, 15, 87000000, '2025-07-05 09:30:00'),
                                                                (2, 13, 91000000, '2025-07-06 15:45:00'),
                                                                (2, 16, 95000000, '2025-07-07 11:00:00');

-- Bids cho item 3 (Tranh Bùi Xuân Phái - ACTIVE)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (3, 17, 202000000, '2025-07-05 11:00:00'),
                                                                (3, 18, 205000000, '2025-07-06 14:20:00'),
                                                                (3, 17, 209000000, '2025-07-07 09:45:00'),
                                                                (3, 11, 213000000, '2025-07-08 16:00:00'),
                                                                (3, 18, 215000000, '2025-07-09 10:30:00');

-- Bids cho item 4 (Land Cruiser - ACTIVE)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (4,  9, 455000000, '2025-07-02 09:00:00'),
                                                                (4, 10, 460000000, '2025-07-03 13:30:00'),
                                                                (4, 12, 465000000, '2025-07-04 10:15:00'),
                                                                (4,  9, 468000000, '2025-07-05 17:00:00');

-- Bids cho item 5 (Macallan 25 - ACTIVE)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (5, 14, 25500000, '2025-07-04 13:00:00'),
                                                                (5, 15, 26500000, '2025-07-05 15:20:00'),
                                                                (5, 16, 27500000, '2025-07-06 11:45:00'),
                                                                (5, 14, 28000000, '2025-07-07 09:10:00');

-- Bids cho item 9 (Omega Speedmaster - CLOSED, người thắng: bidder05 - id=13)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (9, 11, 122000000, '2025-06-01 10:00:00'),
                                                                (9, 12, 126000000, '2025-06-03 14:30:00'),
                                                                (9, 13, 130000000, '2025-06-05 09:15:00'),
                                                                (9, 11, 136000000, '2025-06-08 16:45:00'),
                                                                (9, 14, 142000000, '2025-06-10 11:20:00'),
                                                                (9, 13, 148000000, '2025-06-14 19:55:00');

-- Bids cho item 10 (Vòng cổ ngọc trai - CLOSED, người thắng: bidder02 - id=10)
INSERT INTO bids (item_id, bidder_id, bid_amount, bid_time) VALUES
                                                                (10, 10, 46000000, '2025-06-05 10:30:00'),
                                                                (10, 15, 49000000, '2025-06-08 13:00:00'),
                                                                (10, 10, 52000000, '2025-06-12 08:45:00'),
                                                                (10, 16, 55000000, '2025-06-15 14:30:00'),
                                                                (10, 10, 57000000, '2025-06-19 17:50:00');

-- ============================================================
-- DATA: transactions
-- Chỉ tạo cho phiên CLOSED (item 9, item 10)
-- ============================================================
INSERT INTO transactions (item_id, buyer_id, seller_id, final_price, transaction_time, status) VALUES
                                                                                                   (9,  13, 7, 148000000, '2025-06-15 21:00:00', 'COMPLETED'),
                                                                                                   (10, 10, 8,  57000000, '2025-06-20 19:00:00', 'COMPLETED');

-- ============================================================
-- DATA: notifications
-- Thông báo cho người thắng, người thua, và hệ thống
-- ============================================================
INSERT INTO notifications (user_id, message, is_read, created_at) VALUES

-- Thông báo kết quả phiên CLOSED item 9 (Omega)
(13, 'Chúc mừng! Bạn đã thắng phiên đấu giá "Đồng hồ Omega Speedmaster Moon Watch 1969" với giá 148.000.000đ.', TRUE,  '2025-06-15 21:00:00'),
(11, 'Phiên đấu giá "Đồng hồ Omega Speedmaster Moon Watch 1969" đã kết thúc. Rất tiếc bạn không phải người chiến thắng.', TRUE,  '2025-06-15 21:00:00'),
(12, 'Phiên đấu giá "Đồng hồ Omega Speedmaster Moon Watch 1969" đã kết thúc. Rất tiếc bạn không phải người chiến thắng.', FALSE, '2025-06-15 21:00:00'),
(14, 'Phiên đấu giá "Đồng hồ Omega Speedmaster Moon Watch 1969" đã kết thúc. Rất tiếc bạn không phải người chiến thắng.', TRUE,  '2025-06-15 21:00:00'),
(7,  'Sản phẩm "Đồng hồ Omega Speedmaster Moon Watch 1969" của bạn đã được bán thành công với giá 148.000.000đ.', TRUE,  '2025-06-15 21:00:00'),

-- Thông báo kết quả phiên CLOSED item 10 (Ngọc trai)
(10, 'Chúc mừng! Bạn đã thắng phiên đấu giá "Vòng cổ ngọc trai Akoya" với giá 57.000.000đ.', TRUE,  '2025-06-20 19:00:00'),
(15, 'Phiên đấu giá "Vòng cổ ngọc trai Akoya" đã kết thúc. Rất tiếc bạn không phải người chiến thắng.', FALSE, '2025-06-20 19:00:00'),
(16, 'Phiên đấu giá "Vòng cổ ngọc trai Akoya" đã kết thúc. Rất tiếc bạn không phải người chiến thắng.', TRUE,  '2025-06-20 19:00:00'),
(8,  'Sản phẩm "Vòng cổ ngọc trai Akoya" của bạn đã được bán thành công với giá 57.000.000đ.', TRUE,  '2025-06-20 19:00:00'),

-- Thông báo phiên đang ACTIVE
(9,  'Có người vừa đặt giá cao hơn bạn trong phiên "Đồng hồ Rolex Submariner 2020". Hãy đặt giá lại!', FALSE, '2025-07-04 16:36:00'),
(11, 'Có người vừa đặt giá cao hơn bạn trong phiên "Tranh sơn dầu Phố Hà Nội". Hãy đặt giá lại!', FALSE, '2025-07-09 10:31:00'),

-- Thông báo phiên PENDING sắp mở
(9,  'Phiên đấu giá "Bình gốm Lý triều thế kỷ XII" sẽ mở vào ngày 20/07/2025. Hãy chuẩn bị!', FALSE, '2025-07-15 09:00:00'),
(10, 'Phiên đấu giá "Bình gốm Lý triều thế kỷ XII" sẽ mở vào ngày 20/07/2025. Hãy chuẩn bị!', FALSE, '2025-07-15 09:00:00'),

-- Thông báo phiên CANCELLED
(9,  'Phiên đấu giá "Tranh lụa Thiếu nữ bên hoa huệ" đã bị huỷ. Tiền đặt cọc (nếu có) sẽ được hoàn lại trong 3-5 ngày làm việc.', TRUE, '2025-05-10 10:00:00');
