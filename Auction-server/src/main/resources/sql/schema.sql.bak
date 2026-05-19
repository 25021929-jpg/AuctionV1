CREATE DATABASE IF NOT EXISTS auction_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE auction_db;

DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS auction_sessions;
DROP TABLE IF EXISTS auction_items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       full_name VARCHAR(100) NOT NULL,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       phone VARCHAR(20) NOT NULL,
                       date_of_birth DATE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'BIDDER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE password_reset_tokens (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       user_id BIGINT NOT NULL,
                                       token VARCHAR(255) NOT NULL UNIQUE,
                                       expired_at TIMESTAMP NOT NULL,
                                       used BOOLEAN DEFAULT FALSE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE categories (
                            category_id INT AUTO_INCREMENT PRIMARY KEY,
                            category_name VARCHAR(100) NOT NULL UNIQUE,
                            description VARCHAR(255)
);

CREATE TABLE auction_items (
                               item_id INT AUTO_INCREMENT PRIMARY KEY,
                               seller_id INT NOT NULL,
                               category_id INT NOT NULL,
                               item_name VARCHAR(150) NOT NULL,
                               description TEXT,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_auction_items_seller
                                   FOREIGN KEY (seller_id)
                                       REFERENCES users(user_id),

                               CONSTRAINT fk_auction_items_category
                                   FOREIGN KEY (category_id)
                                       REFERENCES categories(category_id)
);

CREATE TABLE auction_sessions (
                                  auction_id INT AUTO_INCREMENT PRIMARY KEY,
                                  item_id INT NOT NULL,
                                  starting_price DECIMAL(15, 2) NOT NULL,
                                  current_price DECIMAL(15, 2) NOT NULL,
                                  start_time DATETIME NOT NULL,
                                  end_time DATETIME NOT NULL,
                                  status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_auction_sessions_item
                                      FOREIGN KEY (item_id)
                                          REFERENCES auction_items(item_id),

                                  CONSTRAINT chk_auction_price
                                      CHECK (starting_price > 0 AND current_price >= starting_price),

                                  CONSTRAINT chk_auction_time
                                      CHECK (end_time > start_time)
);

CREATE TABLE bids (
                      bid_id INT AUTO_INCREMENT PRIMARY KEY,
                      auction_id INT NOT NULL,
                      bidder_id INT NOT NULL,
                      bid_amount DECIMAL(15, 2) NOT NULL,
                      bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_bids_auction
                          FOREIGN KEY (auction_id)
                              REFERENCES auction_sessions(auction_id),

                      CONSTRAINT fk_bids_bidder
                          FOREIGN KEY (bidder_id)
                              REFERENCES users(user_id),

                      CONSTRAINT chk_bid_amount
                          CHECK (bid_amount > 0)
);