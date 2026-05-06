USE auction_db;

-- Admin mẫu
-- username: admin
-- password: admin123
INSERT INTO users (username, password_hash, full_name, role)
SELECT
    'admin',
    '240be518fabd2724d4d6f04a6b22bb5b326dcd1d8e0c67813837cb5dfc1b5af0',
    'System Admin',
    'ADMIN'
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);