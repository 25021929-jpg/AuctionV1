-- ═══════════════════════════════════════════════════════════════
--  Auction System — Database Schema
--  MySQL 8.0+  |  Charset: utf8mb4  |  Engine: InnoDB
-- ═══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS auction_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auction_db;

-- ───────────────────────────────────────────────────────────────
-- 1. CATEGORIES
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
                                          id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 2. USERS  (Bidder / Seller / Admin — phân biệt bằng role)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
                                     id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,                          -- BCrypt hash
    role          ENUM('BIDDER','SELLER','ADMIN') NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    shop_name     VARCHAR(100),                                   -- Chỉ dùng cho SELLER
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email  (email),
    INDEX idx_users_role   (role),
    INDEX idx_users_active (is_active)
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 3. AUCTION_ITEMS  (thông tin sản phẩm chung)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auction_items (
                                             id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             name           VARCHAR(200)   NOT NULL,
    description    TEXT,
    starting_price DECIMAL(15,2)  NOT NULL,
    item_type      ENUM('ELECTRONICS','ART','VEHICLE') NOT NULL,
    category_id    BIGINT         NOT NULL,
    seller_id      BIGINT         NOT NULL,
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id)   REFERENCES users(id)      ON DELETE RESTRICT,

    INDEX idx_items_seller   (seller_id),
    INDEX idx_items_category (category_id),
    INDEX idx_items_type     (item_type)
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 4. ITEM DETAIL TABLES  (thông tin đặc trưng theo loại)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS electronics_details (
                                                   item_id         BIGINT PRIMARY KEY,
                                                   brand           VARCHAR(100),
    model           VARCHAR(100),
    warranty_months INT DEFAULT 0,

    FOREIGN KEY (item_id) REFERENCES auction_items(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS art_details (
                                           item_id BIGINT PRIMARY KEY,
                                           artist  VARCHAR(200),
    year    INT,
    medium  VARCHAR(100),         -- Sơn dầu, Màu nước, Điêu khắc...

    FOREIGN KEY (item_id) REFERENCES auction_items(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vehicle_details (
                                               item_id      BIGINT PRIMARY KEY,
                                               manufacturer VARCHAR(100),
    year         INT,
    mileage      INT DEFAULT 0,   -- km
    fuel_type    VARCHAR(50),     -- Xăng, Điện, Hybrid, Dầu

    FOREIGN KEY (item_id) REFERENCES auction_items(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 5. AUCTION_SESSIONS  (phiên đấu giá)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auction_sessions (
                                                id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                item_id               BIGINT         NOT NULL UNIQUE,         -- 1 item chỉ có 1 phiên tại 1 thời điểm
                                                status                ENUM('OPEN','RUNNING','FINISHED','PAID','CANCELED')
    NOT NULL DEFAULT 'OPEN',
    current_highest_price DECIMAL(15,2)  NOT NULL,
    current_winner_id     BIGINT,                                 -- NULL nếu chưa có bid
    start_time            DATETIME       NOT NULL,
    end_time              DATETIME       NOT NULL,
    extension_count       INT            NOT NULL DEFAULT 0,
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (item_id)           REFERENCES auction_items(id) ON DELETE RESTRICT,
    FOREIGN KEY (current_winner_id) REFERENCES users(id)         ON DELETE SET NULL,

    -- Index quan trọng: scheduler query phiên hết giờ
    INDEX idx_sessions_status_end  (status, end_time),
    INDEX idx_sessions_status_start(status, start_time),
    INDEX idx_sessions_winner      (current_winner_id)
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 6. BIDS  (lịch sử đặt giá)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bids (
                                    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    session_id BIGINT        NOT NULL,
                                    bidder_id  BIGINT        NOT NULL,
                                    amount     DECIMAL(15,2) NOT NULL,
    placed_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     ENUM('VALID','OUTBID','WINNER','REFUNDED') NOT NULL DEFAULT 'VALID',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES auction_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id)  REFERENCES users(id)            ON DELETE RESTRICT,

    -- Index quan trọng nhất: tìm top bid theo phiên
    INDEX idx_bids_session_amount (session_id, amount DESC),
    INDEX idx_bids_bidder         (bidder_id),
    INDEX idx_bids_placed_at      (session_id, placed_at DESC)
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 7. AUTO_BIDS  (đăng ký đấu giá tự động)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auto_bids (
                                         id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         session_id    BIGINT        NOT NULL,
                                         bidder_id     BIGINT        NOT NULL,
                                         max_bid       DECIMAL(15,2) NOT NULL,
    increment     DECIMAL(15,2) NOT NULL DEFAULT 1000,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    registered_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES auction_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id)  REFERENCES users(id)            ON DELETE CASCADE,

    UNIQUE KEY uk_auto_bid (session_id, bidder_id),              -- 1 bidder chỉ 1 auto-bid/phiên
    INDEX idx_auto_bids_session (session_id, is_active)
    ) ENGINE=InnoDB;

-- ───────────────────────────────────────────────────────────────
-- 8. NOTIFICATIONS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
                                             id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id            BIGINT       NOT NULL,
                                             message            TEXT         NOT NULL,
                                             type               ENUM('BID_PLACED','OUTBID','AUCTION_WON','AUCTION_LOST',
                                             'AUCTION_EXTENDED','AUCTION_CLOSED','SYSTEM_MESSAGE')
    NOT NULL,
    is_read            BOOLEAN      NOT NULL DEFAULT FALSE,
    related_session_id BIGINT,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_notif_user_read (user_id, is_read),
    INDEX idx_notif_created   (user_id, created_at DESC)
    ) ENGINE=InnoDB;