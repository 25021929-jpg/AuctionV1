-- ═══════════════════════════════════════════════════════════════
--  Auction System — Seed Data (dev.mode only)
-- ═══════════════════════════════════════════════════════════════

USE auction_db;

-- ───────────────────────────────────────────────────────────────
-- Categories
-- ───────────────────────────────────────────────────────────────
INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Electronics',   'Thiết bị điện tử, công nghệ'),
(2, 'Art',           'Tác phẩm nghệ thuật, hội hoạ, điêu khắc'),
(3, 'Vehicle',       'Xe hơi, xe máy, phương tiện'),
(4, 'Collectibles',  'Đồ sưu tầm, cổ vật');

-- ───────────────────────────────────────────────────────────────
-- Users
-- password = "Admin@123"  →  BCrypt hash bên dưới
-- Để tạo hash mới: chạy PasswordUtil.hash("Admin@123") hoặc
--   dùng online BCrypt generator với cost=12
-- ───────────────────────────────────────────────────────────────
INSERT IGNORE INTO users (id, username, email, password_hash, role, is_active) VALUES
(1, 'admin',    'admin@auction.vn',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LQ.H7XiM7PiqGUGda',
    'ADMIN', TRUE),
(2, 'seller1',  'seller1@auction.vn',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LQ.H7XiM7PiqGUGda',
    'SELLER', TRUE),
(3, 'bidder1',  'bidder1@auction.vn',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LQ.H7XiM7PiqGUGda',
    'BIDDER', TRUE),
(4, 'bidder2',  'bidder2@auction.vn',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LQ.H7XiM7PiqGUGda',
    'BIDDER', TRUE);

UPDATE users SET shop_name = 'Tech Store VN' WHERE id = 2;

-- ───────────────────────────────────────────────────────────────
-- Sample auction items + sessions (chỉ để demo)
-- ───────────────────────────────────────────────────────────────
INSERT IGNORE INTO auction_items (id, name, description, starting_price, item_type, category_id, seller_id)
VALUES
(1, 'MacBook Pro M3 14"',
    'Laptop Apple MacBook Pro M3 14 inch, RAM 16GB, SSD 512GB, màu Bạc',
    25000000, 'ELECTRONICS', 1, 2),
(2, 'Tranh Sơn Dầu "Hoàng Hôn Hạ Long"',
    'Tác phẩm của hoạ sĩ Nguyễn Văn An, vẽ năm 2020, kích thước 80x60cm',
    5000000,  'ART', 2, 2),
(3, 'Toyota Camry 2022',
    'Xe Toyota Camry 2.5Q 2022, màu Đen, chạy 30.000km, còn bảo hành',
    900000000,'VEHICLE', 3, 2);

INSERT IGNORE INTO electronics_details (item_id, brand, model, warranty_months)
VALUES (1, 'Apple', 'MacBook Pro M3', 12);

INSERT IGNORE INTO art_details (item_id, artist, year, medium)
VALUES (2, 'Nguyễn Văn An', 2020, 'Sơn dầu');

INSERT IGNORE INTO vehicle_details (item_id, manufacturer, year, mileage, fuel_type)
VALUES (3, 'Toyota', 2022, 30000, 'Xăng');

-- Phiên đang mở (bắt đầu 1 phút sau khi chạy)
INSERT IGNORE INTO auction_sessions
    (id, item_id, status, current_highest_price, start_time, end_time)
VALUES
(1, 1, 'RUNNING', 25000000,
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    DATE_ADD(NOW(),  INTERVAL 1 HOUR)),
(2, 2, 'OPEN',    5000000,
    DATE_ADD(NOW(), INTERVAL 10 MINUTE),
    DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(3, 3, 'OPEN',    900000000,
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    DATE_ADD(NOW(), INTERVAL 3 HOUR));