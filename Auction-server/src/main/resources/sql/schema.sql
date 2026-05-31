CREATE DATABASE IF NOT EXISTS auction_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE auction_db;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS wallet_transactions;
DROP TABLE IF EXISTS item_images;
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS auction_sessions;
DROP TABLE IF EXISTS auction_items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       full_name     VARCHAR(100) NOT NULL,
                       email         VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role          ENUM('ADMIN','SELLER','BIDDER') NOT NULL DEFAULT 'BIDDER',
                       balance       DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
                       phone         VARCHAR(20),
                       date_of_birth DATE NOT NULL,
                       avatar_url    VARCHAR(500),
                       is_active     TINYINT(1)   NOT NULL DEFAULT 1,
                       created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       balance_on_hold DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (balance_on_hold >= 0) -- Số tiền đang giữ cho các phiên đấu giá đang tham gia
);


CREATE TABLE categories (
                            category_id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            category_name        VARCHAR(100) NOT NULL,
                            slug        VARCHAR(100)     NOT NULL UNIQUE,
                            description TEXT,
                            parent_id   INT UNSIGNED,
                            sort_order  INT NOT NULL DEFAULT 0,
                            FOREIGN KEY (parent_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- Dữ liệu nền bắt buộc để seller có thể đăng sản phẩm ngay sau khi tạo schema.
-- SellerService kiểm tra categoryId phải tồn tại trước khi tạo AuctionItem/AuctionSession.
INSERT INTO categories (category_id, category_name, slug, description, sort_order)
VALUES
    (1, 'Electronics', 'electronics', 'Electronic devices and accessories', 1),
    (2, 'Art', 'art', 'Artworks and collectibles', 2),
    (3, 'Vehicle', 'vehicle', 'Vehicles and transportation items', 3);
-- ============================================================
-- 3. BẢNG ITEMS (hàng hóa)
-- ============================================================

CREATE TABLE auction_items (
                               item_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               seller_id BIGINT UNSIGNED NOT NULL,
                               category_id INT UNSIGNED NOT NULL,
                               item_name VARCHAR(255) NOT NULL,
                               description TEXT,
                               `condition`   ENUM('NEW','LIKE_NEW','GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD', -- BẮT BUỘC phải có dấu huyền ở đây vì condition là một từ khóa của MySQL
                               status      ENUM('DRAFT','PENDING_REVIEW','APPROVED','REJECTED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                               CONSTRAINT fk_auction_items_seller
                                   FOREIGN KEY (seller_id)
                                       REFERENCES users(id) ON DELETE RESTRICT,

                               CONSTRAINT fk_auction_items_category
                                   FOREIGN KEY (category_id)
                                       REFERENCES categories(category_id) ON DELETE RESTRICT
);
CREATE TABLE item_images (
                             id          BIGINT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
                             item_id     BIGINT UNSIGNED  NOT NULL,
                             image_url   VARCHAR(500)     NOT NULL,
                             is_primary  TINYINT(1)       NOT NULL DEFAULT 0,
                             sort_order  INT              NOT NULL DEFAULT 0,
                             FOREIGN KEY (item_id) REFERENCES auction_items(item_id) ON DELETE CASCADE
);
-- ============================================================
-- 4. BẢNG AUCTIONS (phiên đấu giá) — BẢNG QUAN TRỌNG NHẤT
-- ============================================================
CREATE TABLE auction_sessions (
                                  auction_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  item_id BIGINT UNSIGNED NOT NULL UNIQUE, -- ✓ 1 item chỉ 1 phiên tại 1 thời điểm
                                  starting_price DECIMAL(15, 2) NOT NULL,
                                  current_price DECIMAL(15, 2) NOT NULL,
                                  min_bid_step    DECIMAL(15,2)    NOT NULL DEFAULT 1000.00,
                                  start_time DATETIME NOT NULL,
                                  end_time DATETIME NOT NULL,
                                  winner_id       BIGINT UNSIGNED,                    -- NULL cho đến khi kết thúc
                                  total_bids      INT UNSIGNED     NOT NULL DEFAULT 0, -- đếm cache, tránh COUNT(*)
                                  status          ENUM('SCHEDULED','ACTIVE','ENDED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
                                  version         INT UNSIGNED     NOT NULL DEFAULT 0, -- cho Optimistic Locking
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_auction_sessions_item
                                      FOREIGN KEY (item_id)
                                          REFERENCES auction_items(item_id) ON DELETE RESTRICT,

                                  CONSTRAINT chk_auction_price
                                      CHECK (starting_price > 0 AND current_price >= starting_price),

                                  CONSTRAINT fk_auction_winner
                                      FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL,

                                  CONSTRAINT chk_auction_time
                                      CHECK (end_time > start_time)
);
-- ============================================================
-- 5. BẢNG BIDS (lịch sử đặt giá)
-- ============================================================

CREATE TABLE bids (
                      bid_id BIGINT  UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                      auction_id BIGINT UNSIGNED NOT NULL,
                      bidder_id BIGINT UNSIGNED NOT NULL,
                      bid_amount DECIMAL(15, 2) NOT NULL,
                      bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      is_winning  TINYINT(1)      NOT NULL DEFAULT 0,  -- 1 = đang giữ giá cao nhất
                      ip_address  VARCHAR(45),                          -- ghi log chống gian lận
                      CONSTRAINT fk_bids_auction
                          FOREIGN KEY (auction_id)
                              REFERENCES auction_sessions(auction_id) ON DELETE RESTRICT,

                      CONSTRAINT fk_bids_bidder
                          FOREIGN KEY (bidder_id)
                              REFERENCES users(id) ON DELETE RESTRICT,

                      CONSTRAINT chk_bid_amount
                          CHECK (bid_amount > 0)
);
-- ============================================================
-- 6. BẢNG PAYMENTS (thanh toán)
-- ============================================================
CREATE TABLE payments (
                          id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          auction_id      BIGINT UNSIGNED  NOT NULL UNIQUE,  -- 1 auction = 1 payment
                          buyer_id        BIGINT UNSIGNED  NOT NULL,
                          seller_id       BIGINT UNSIGNED  NOT NULL,
                          amount          DECIMAL(15,2)    NOT NULL,
                          platform_fee    DECIMAL(15,2)    NOT NULL DEFAULT 0.00,  -- phí sàn
                          status          ENUM('PENDING','COMPLETED','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
                          method          ENUM('WALLET','BANK_TRANSFER','MOMO','VNPAY') NOT NULL,
                          transaction_ref VARCHAR(100) UNIQUE,   -- mã giao dịch từ cổng thanh toán
                          paid_at         TIMESTAMP,
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (auction_id) REFERENCES auction_sessions(auction_id) ON DELETE RESTRICT,
                          FOREIGN KEY (buyer_id)   REFERENCES users(id)    ON DELETE RESTRICT,
                          FOREIGN KEY (seller_id)  REFERENCES users(id)    ON DELETE RESTRICT
);

-- ============================================================
-- 7. BẢNG WALLET_TRANSACTIONS (lịch sử biến động số dư)
-- ============================================================
CREATE TABLE wallet_transactions (
                          transaction_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          user_id        BIGINT UNSIGNED NOT NULL,
                          auction_id     BIGINT UNSIGNED,
                          type           ENUM('DEPOSIT','AUCTION_PAYMENT','AUCTION_RECEIVE') NOT NULL,
                          amount         DECIMAL(15,2) NOT NULL,
                          balance_after  DECIMAL(15,2) NOT NULL CHECK (balance_after >= 0),
                          description    VARCHAR(500),
                          created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                          FOREIGN KEY (auction_id) REFERENCES auction_sessions(auction_id) ON DELETE SET NULL
);

--
-- INDEX để tăng tốc truy vấn thường dùng
--
-- ============================================================
-- INDEX: users
-- ============================================================

-- Index 1: Đăng nhập bằng email
-- Lý do: mỗi request login đều WHERE email = ? → query cực kỳ thường xuyên
-- email đã là UNIQUE nên MySQL TỰ TẠO index này rồi → KHÔNG cần viết thêm
-- (UNIQUE constraint = UNIQUE index, dùng được như index bình thường)
-- username cũng vậy — UNIQUE tự có index rồi, không cần thêm
-- Index 2: Lọc user theo role + trạng thái (dùng cho admin panel)
CREATE INDEX idx_wallet_transactions_user_time
    ON wallet_transactions(user_id, created_at);

CREATE INDEX idx_users_role_active
    ON users(role, is_active);
-- Lý do: Admin thường query "Danh sách SELLER đang active"
-- WHERE role = 'SELLER' AND is_active = 1
-- role đặt trước (equality), is_active đặt sau (cũng equality nhưng cardinality thấp hơn)
-- Lợi ích: tránh full scan bảng users khi lọc theo role

-- Index 3: Sắp xếp user mới nhất
CREATE INDEX idx_users_created
    ON users(created_at DESC);
-- Lý do: Admin xem "user đăng ký gần đây" → ORDER BY created_at DESC LIMIT 20
-- Không có index → MySQL phải sort toàn bộ bảng (filesort)
-- Có index → MySQL đọc index từ cuối, lấy đúng 20 dòng, dừng ngay

-- ============================================================
-- INDEX: password_reset_tokens
-- ============================================================

-- token đã là UNIQUE → tự có index, dùng cho WHERE token = ? khi xác thực


-- ============================================================
-- INDEX: categories
-- ============================================================

-- category_id là PRIMARY KEY → tự có Clustered Index

-- Index 1: Lấy danh mục con của một danh mục cha
CREATE INDEX idx_categories_parent
    ON categories(parent_id, sort_order);
-- Lý do: Render menu navigation → SELECT * FROM categories WHERE parent_id = 5 ORDER BY sort_order
-- parent_id: equality (= 5), sort_order: ORDER BY
-- Cả hai cột trong index → MySQL không cần filesort thêm
-- Lợi ích: menu load nhanh dù có hàng trăm danh mục con

-- ============================================================
-- INDEX: item_images
-- ============================================================

-- Index 1: Lấy ảnh của một item
CREATE INDEX idx_images_item_primary
    ON item_images(item_id, is_primary);
-- Lý do: 2 query thường gặp nhất:
--   1. SELECT * FROM item_images WHERE item_id = 5          → lấy tất cả ảnh
--   2. SELECT * FROM item_images WHERE item_id = 5 AND is_primary = 1  → lấy ảnh đại diện
-- item_id đặt trước (cardinality cao, equality)
-- is_primary đặt sau (cardinality thấp — chỉ 0/1, nhưng kết hợp với item_id thì hiệu quả)
-- Covering index: item_id + is_primary đủ để MySQL biết cần lấy dòng nào
-- Lợi ích: mỗi lần hiển thị thumbnail không cần full scan bảng ảnh
-- Dòng này mà cho is_primary lên trước thì MySQL sẽ không dùng index khi query lấy tất cả ảnh của item_id = 5, vì is_primary không có giá trị cụ thể (có thể là 0 hoặc 1) → full scan bảng ảnh mỗi lần hiển thị tất cả các ảnh của item đó


-- ============================================================
-- INDEX: auction_sessions
-- ============================================================

-- auction_id là PRIMARY KEY → tự có Clustered Index
-- item_id đã có UNIQUE → tự có UNIQUE Index (dùng cho WHERE item_id = ?)

-- Index 1: Trang chủ — danh sách phiên đang active, sắp hết hạn lên đầu
CREATE INDEX idx_sessions_status_endtime
    ON auction_sessions(status, end_time);
-- Lý do: Query quan trọng nhất của toàn app:
-- WHERE status = 'ACTIVE' AND end_time > NOW() ORDER BY end_time ASC
-- status: equality (đặt trước, dù cardinality thấp nhưng lọc mạnh: 4 giá trị ENUM)
-- end_time: range + ORDER BY (đặt sau)
-- Lợi ích: trang chủ load nhanh kể cả khi có hàng triệu phiên lịch sử
-- Không có index này → full scan toàn bộ bảng auction_sessions mỗi lần load trang chủ

-- Index 2: Scheduled Task tìm phiên hết giờ để đóng
CREATE INDEX idx_sessions_status_scheduled
    ON auction_sessions(status, start_time);
-- Lý do: @Scheduled job chạy mỗi phút:
-- UPDATE auction_sessions SET status='ACTIVE' WHERE status='SCHEDULED' AND start_time <= NOW()
-- Kết hợp với idx_sessions_status_endtime xử lý cả 2 loại scheduled job


-- ============================================================
-- INDEX: bids
-- ============================================================

-- bid_id là PRIMARY KEY → tự có Clustered Index

-- Index 1: Xem lịch sử đặt giá của một phiên (real-time feed)
CREATE INDEX idx_bids_auction_time
    ON bids(auction_id, bid_time DESC);
-- Lý do: Query chạy liên tục mỗi khi có người xem phiên đấu giá:
-- SELECT * FROM bids WHERE auction_id = 7 ORDER BY bid_time DESC LIMIT 10
-- auction_id: equality (đặt trước, cardinality cao)
-- bid_time DESC: ORDER BY (đặt sau, khớp hướng DESC → không filesort)
-- Lợi ích: 10 bid gần nhất lấy ngay lập tức, không scan toàn bảng
-- Đây là index QUAN TRỌNG NHẤT vì bảng bids sẽ có hàng chục triệu dòng

-- Index 2: Lịch sử đặt giá của một user (trang profile)
CREATE INDEX idx_bids_bidder_time
    ON bids(bidder_id, bid_time DESC);
-- Lý do: User xem "lịch sử đặt giá của tôi"
-- SELECT * FROM bids WHERE bidder_id = 42 ORDER BY bid_time DESC LIMIT 20
-- bidder_id: equality (cardinality cao)
-- bid_time DESC: ORDER BY

-- Index 3: Tìm bid đang thắng của một phiên (cập nhật winner)
CREATE INDEX idx_bids_auction_winning
    ON bids(auction_id, is_winning);
-- Lý do: Khi có bid mới, cần tìm bid cũ đang thắng để set is_winning = 0
-- UPDATE bids SET is_winning = 0 WHERE auction_id = 7 AND is_winning = 1
-- Không có index → scan toàn bộ bids của phiên đó mỗi lần có người đặt giá
-- auction_id đặt trước (cardinality cao), is_winning sau (chỉ 0/1)

-- Index 4: Covering index cho query thống kê nhanh
CREATE INDEX idx_bids_auction_amount
    ON bids(auction_id, bid_amount DESC, bidder_id);
-- Lý do: Query "top bidder của một phiên" hoặc "giá cao nhất hiện tại"
-- SELECT bidder_id, MAX(bid_amount) FROM bids WHERE auction_id = 7
-- Covering index: MySQL đọc chỉ từ index, không cần đọc data page
-- Lợi ích: thống kê nhanh, không tốn I/O vào data pages

-- ============================================================
-- INDEX: payments
-- ============================================================

-- id là PRIMARY KEY → tự có Clustered Index
-- auction_id đã có UNIQUE → tự có UNIQUE Index
-- transaction_ref đã có UNIQUE → tự có UNIQUE Index

-- Index 1: Lịch sử mua của buyer
CREATE INDEX idx_payments_buyer_status
    ON payments(buyer_id, status, created_at DESC);
-- Lý do: Buyer xem "lịch sử thanh toán của tôi"
-- WHERE buyer_id = 42 AND status = 'COMPLETED' ORDER BY created_at DESC
-- buyer_id: equality + cardinality cao (đặt đầu)
-- status: equality (đặt giữa, lọc theo PENDING/COMPLETED/FAILED/REFUNDED)
-- created_at: ORDER BY (đặt cuối, tránh filesort)

-- Index 2: Lịch sử bán của seller (doanh thu)
CREATE INDEX idx_payments_seller_status
    ON payments(seller_id, status, created_at DESC);
-- Lý do: Seller xem "doanh thu của tôi"
-- WHERE seller_id = 99 AND status = 'COMPLETED'
-- Tương tự buyer nhưng góc nhìn seller

-- Index 3: Tìm payment chưa xử lý (admin + cron job)
CREATE INDEX idx_payments_status_created
    ON payments(status, created_at);
-- Lý do: Cron job kiểm tra payment PENDING quá 24h
-- WHERE status = 'PENDING' AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)
-- status: equality (đặt trước)
-- created_at: range (đặt sau)
