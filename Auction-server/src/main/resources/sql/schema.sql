-- 1) Tạo DB nếu cần và chuyển vào DB
CREATE DATABASE IF NOT EXISTS auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_db;

-- 2) Tạo bảng users (an toàn: IF NOT EXISTS)
CREATE TABLE IF NOT EXISTS users (
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

-- 3) Chèn vài user mẫu (password đã được bcrypt hashed)
INSERT INTO users (username, password_hash, full_name, role, email, phone, date_of_birth)
VALUES
    ('admin',  'ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Admin User',       'ADMIN',  'admin@example.com',  '0123456789', '1980-01-01'),
    ('seller01','ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Nguyen Van Seller','SELLER','seller01@example.com','0987654321','1990-02-02'),
    ('bidder01','ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Tran Van Bidder',  'BIDDER','bidder01@example.com','0912345678','1995-03-03');


-- 4) Kiểm tra
SELECT id, username, role, email FROM users;