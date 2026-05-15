USE auction_db;

-- =====================================================
-- SAMPLE USERS
-- Password gốc của tất cả user dưới đây:
-- 123456
-- =====================================================

INSERT INTO users (
    full_name,
    username,
    email,
    phone,
    date_of_birth,
    password_hash,
    role
)
VALUES
    (
        'Vu Hoang',
        'hoang123',
        'hoang@gmail.com',
        '0987654321',
        '2005-01-20',

        -- password = 123456
        'demo_salt:demo_hash',

        'BIDDER'
    ),

    (
        'Nguyen Van A',
        'vana',
        'vana@gmail.com',
        '0911111111',
        '2000-05-10',

        -- password = 123456
        'demo_salt:demo_hash',

        'SELLER'
    ),

    (
        'Admin System',
        'admin',
        'admin@gmail.com',
        '0900000000',
        '1999-01-01',

        -- password = 123456
        'demo_salt:demo_hash',

        'ADMIN'
    );

-- =====================================================
-- SAMPLE RESET TOKEN
-- =====================================================

INSERT INTO password_reset_tokens (
    user_id,
    token,
    expired_at,
    used
)
VALUES
    (
        1,
        'sample-reset-token-123',

        DATE_ADD(NOW(), INTERVAL 15 MINUTE),

        FALSE
    );