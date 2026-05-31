USE auction_db;

-- Dùng khi không muốn DROP DATABASE. Nếu đã chạy schema.sql mới thì không cần file này.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS balance DECIMAL(15,2) NOT NULL DEFAULT 0.00;

CREATE TABLE IF NOT EXISTS wallet_transactions (
    transaction_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NOT NULL,
    auction_id     BIGINT UNSIGNED,
    type           ENUM('DEPOSIT','AUCTION_PAYMENT','AUCTION_RECEIVE') NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    description    VARCHAR(500),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (auction_id) REFERENCES auction_sessions(auction_id) ON DELETE SET NULL
);

CREATE INDEX idx_wallet_transactions_user_time
    ON wallet_transactions(user_id, created_at);
