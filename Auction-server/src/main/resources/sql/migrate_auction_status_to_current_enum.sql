USE auction_db;

-- Dùng khi không muốn DROP DATABASE. Nếu đã reset DB bằng schema.sql mới thì không cần chạy file này.
-- Chuẩn mới của phiên đấu giá: SCHEDULED, ACTIVE, ENDED, CANCELED.
-- Các trạng thái cũ được map như sau:
-- OPEN -> SCHEDULED, RUNNING -> ACTIVE, FINISHED/PAID -> ENDED, CANCELLED -> CANCELED.

ALTER TABLE auction_sessions
    MODIFY COLUMN status VARCHAR(15) NOT NULL DEFAULT 'SCHEDULED';

UPDATE auction_sessions SET status = 'SCHEDULED' WHERE status = 'OPEN';
UPDATE auction_sessions SET status = 'ACTIVE' WHERE status = 'RUNNING';
UPDATE auction_sessions SET status = 'ENDED' WHERE status IN ('FINISHED', 'PAID');
UPDATE auction_sessions SET status = 'CANCELED' WHERE status = 'CANCELLED';

ALTER TABLE auction_sessions
    MODIFY COLUMN status ENUM('SCHEDULED','ACTIVE','ENDED','CANCELED') NOT NULL DEFAULT 'SCHEDULED';
