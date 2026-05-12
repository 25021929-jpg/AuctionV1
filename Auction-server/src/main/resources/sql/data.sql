USE auction_db;

INSERT INTO users (username, password_hash, full_name, role)
VALUES
    ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiMnAmxS5hGaeyX66M52YjU3lQ7J6hG', 'Admin User', 'ADMIN'),
    ('seller01', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiMnAmxS5hGaeyX66M52YjU3lQ7J6hG', 'Nguyen Van Seller', 'SELLER'),
    ('bidder01', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiMnAmxS5hGaeyX66M52YjU3lQ7J6hG', 'Tran Van Bidder', 'BIDDER');

INSERT INTO categories (category_name, description)
VALUES
    ('Điện thoại', 'Các sản phẩm điện thoại di động'),
    ('Laptop', 'Máy tính xách tay'),
    ('Đồng hồ', 'Đồng hồ thông minh và đồng hồ cơ'),
    ('Xe máy', 'Các loại xe máy đã qua sử dụng');

INSERT INTO auction_items (seller_id, category_id, item_name, description)
VALUES
    (2, 1, 'iPhone 13 Pro Max', 'Máy đẹp 95%, pin tốt, đầy đủ phụ kiện'),
    (2, 2, 'Laptop Dell XPS 13', 'Laptop mỏng nhẹ, RAM 16GB, SSD 512GB'),
    (2, 3, 'Apple Watch Series 7', 'Đồng hồ còn đẹp, hoạt động tốt');

INSERT INTO auction_sessions (
    item_id,
    starting_price,
    current_price,
    start_time,
    end_time,
    status
)
VALUES
    (1, 5000000, 5000000, '2026-05-08 09:00:00', '2026-05-09 21:00:00', 'ACTIVE'),
    (2, 12000000, 12000000, '2026-05-10 09:00:00', '2026-05-12 21:00:00', 'UPCOMING'),
    (3, 3000000, 3500000, '2026-05-07 09:00:00', '2026-05-08 21:00:00', 'ACTIVE');

INSERT INTO bids (auction_id, bidder_id, bid_amount, bid_time)
VALUES
    (3, 3, 3500000, '2026-05-08 10:00:00');