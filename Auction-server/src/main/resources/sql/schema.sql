-- ============================================================
-- SQL Schema cho feature auction
-- Chạy file này trong MySQL để tạo các bảng cần thiết
-- ============================================================

-- Bảng sản phẩm đấu giá
CREATE TABLE IF NOT EXISTS items (
                                     item_id       INT AUTO_INCREMENT PRIMARY KEY,
                                     name          VARCHAR(255)   NOT NULL,
                                     description   TEXT,
                                     category      VARCHAR(50)    NOT NULL,  -- ELECTRONICS / ART / VEHICLE / OTHER
                                     seller_id     INT            NOT NULL,
                                     created_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (seller_id) REFERENCES users(user_id)
);

-- Bảng phiên đấu giá
CREATE TABLE IF NOT EXISTS auctions (
                                        auction_id          INT AUTO_INCREMENT PRIMARY KEY,
                                        item_id             INT            NOT NULL,
                                        seller_id           INT            NOT NULL,
                                        starting_price      DECIMAL(15,2)  NOT NULL,
                                        current_price       DECIMAL(15,2)  NOT NULL,
                                        current_winner_id   INT            NULL,        -- NULL nếu chưa có bid
                                        start_time          DATETIME       NOT NULL,
                                        end_time            DATETIME       NOT NULL,
                                        status              VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
    -- OPEN | RUNNING | FINISHED | CANCELED
                                        created_at          DATETIME       DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (item_id)           REFERENCES items(item_id),
                                        FOREIGN KEY (seller_id)         REFERENCES users(user_id),
                                        FOREIGN KEY (current_winner_id) REFERENCES users(user_id)
);

-- Bảng lịch sử bid (dùng cho BidHistory chart và BiddingService)
CREATE TABLE IF NOT EXISTS bids (
                                    bid_id      INT AUTO_INCREMENT PRIMARY KEY,
                                    auction_id  INT            NOT NULL,
                                    bidder_id   INT            NOT NULL,
                                    amount      DECIMAL(15,2)  NOT NULL,
                                    bid_time    DATETIME       DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id),
                                    FOREIGN KEY (bidder_id)  REFERENCES users(user_id)
);

-- ============================================================
-- Dữ liệu mẫu để test
-- ============================================================

-- Thêm item mẫu (giả sử đã có users với user_id=1,2)
INSERT INTO items (name, description, category, seller_id)
VALUES
    ('iPhone 15 Pro', 'Điện thoại Apple mới 100%', 'ELECTRONICS', 1),
    ('Tranh Sơn Dầu Phong Cảnh', 'Tác phẩm nghệ thuật độc đáo', 'ART', 2),
    ('Toyota Camry 2022', 'Xe ô tô còn mới, ít đi', 'VEHICLE', 1);

-- Thêm phiên đấu giá mẫu
INSERT INTO auctions (item_id, seller_id, starting_price, current_price, start_time, end_time, status)
VALUES
    (1, 1, 25000000, 25000000, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'RUNNING'),
    (2, 2, 5000000,  5000000,  DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR), 'OPEN'),
    (3, 1, 500000000, 500000000, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'FINISHED');
