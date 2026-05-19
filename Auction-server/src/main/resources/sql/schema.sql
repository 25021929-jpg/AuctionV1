CREATE DATABASE IF NOT EXISTS auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_db;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
                                     id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
                                     full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'BIDDER',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
    );

-- ============================================================
-- TABLE: categories, danh mục sản phẩm
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
                                          id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
                                          category_name VARCHAR(100)  NOT NULL,
    description   TEXT
    );

-- ============================================================
-- TABLE: auction_items  sản phẩm đấu giá
-- ============================================================
CREATE TABLE IF NOT EXISTS auction_items (
                                             id               BIGINT         PRIMARY KEY AUTO_INCREMENT,
                                             seller_id        BIGINT         NOT NULL,
                                             category_id      BIGINT,
                                             item_name        VARCHAR(200)   NOT NULL,
    description      TEXT,
    starting_price   DECIMAL(15, 2) NOT NULL,
    current_price    DECIMAL(15, 2) NOT NULL,
    min_bid_increment DECIMAL(15, 2) NOT NULL DEFAULT 1000,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    start_time       TIMESTAMP      NOT NULL,
    end_time         TIMESTAMP      NOT NULL,
    created_at       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_seller   FOREIGN KEY (seller_id)   REFERENCES users(id),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES categories(id)
    );

-- ============================================================
-- TABLE: bids  lịch sử đặt giá
-- ============================================================
CREATE TABLE IF NOT EXISTS bids (
                                    id          BIGINT         PRIMARY KEY AUTO_INCREMENT,
                                    item_id     BIGINT         NOT NULL,
                                    bidder_id   BIGINT         NOT NULL,
                                    bid_amount  DECIMAL(15, 2) NOT NULL,
    bid_time    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bid_item   FOREIGN KEY (item_id)   REFERENCES auction_items(id),
    CONSTRAINT fk_bid_bidder FOREIGN KEY (bidder_id) REFERENCES users(id)
    );

-- ============================================================
-- TABLE: transactions  giao dịch thành công
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
                                            id               BIGINT         PRIMARY KEY AUTO_INCREMENT,
                                            item_id          BIGINT         NOT NULL,
                                            buyer_id         BIGINT         NOT NULL,
                                            seller_id        BIGINT         NOT NULL,
                                            final_price      DECIMAL(15, 2) NOT NULL,
    transaction_time TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_tx_item   FOREIGN KEY (item_id)   REFERENCES auction_items(id),
    CONSTRAINT fk_tx_buyer  FOREIGN KEY (buyer_id)  REFERENCES users(id),
    CONSTRAINT fk_tx_seller FOREIGN KEY (seller_id) REFERENCES users(id)
    );

-- ============================================================
-- TABLE: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
                                             id         BIGINT    PRIMARY KEY AUTO_INCREMENT,
                                             user_id    BIGINT    NOT NULL,
                                             message    TEXT      NOT NULL,
                                             is_read    BOOLEAN   DEFAULT FALSE,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
