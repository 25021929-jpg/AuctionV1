CREATE DATABASE IF NOT EXISTS auction_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE auction_db;

DROP TABLE IF EXISTS password_reset_tokens;
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

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_tokens (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                       user_id BIGINT NOT NULL,
                                       token VARCHAR(255) NOT NULL UNIQUE,

                                       expired_at TIMESTAMP NOT NULL,
                                       used BOOLEAN NOT NULL DEFAULT FALSE,

                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_password_reset_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_reset_user_id ON password_reset_tokens(user_id);