-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS auction_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Chọn database để sử dụng
USE auction_db;

-- Xóa bảng users nếu muốn tạo lại từ đầu
-- Cẩn thận: dòng này sẽ xóa toàn bộ dữ liệu users cũ
-- DROP TABLE IF EXISTS users;

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    -- ID tự tăng, dùng làm khóa chính
                                     id INT AUTO_INCREMENT PRIMARY KEY,

    -- Tên đăng nhập, không được trùng
                                     username VARCHAR(50) NOT NULL UNIQUE,

    -- Mật khẩu đã mã hóa, không lưu mật khẩu thật
    password_hash VARCHAR(255) NOT NULL,

    -- Họ tên người dùng
    full_name VARCHAR(100) NOT NULL,

    -- Vai trò người dùng: BIDDER, SELLER, ADMIN
    role VARCHAR(20) NOT NULL DEFAULT 'BIDDER',

    -- Thời gian tạo tài khoản
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Thời gian cập nhật tài khoản
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );