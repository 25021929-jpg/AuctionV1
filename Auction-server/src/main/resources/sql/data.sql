USE auction_db;

INSERT INTO users (username, password_hash, full_name, role, email, phone, date_of_birth)
VALUES
    ('admin', 'ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Admin User', 'ADMIN', 'admin@example.com', '0123456789', '1980-01-01'),
    ('seller01', 'ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Seller Demo', 'SELLER', 'seller01@example.com', '0987654321', '1990-02-02'),
    ('bidder01', 'ln7a1TqdGii7gKo19sWXAQ==:4Aos7iCIr/JoBVMLVaBoVmMGdUesbITgsM1SYZctoug=', 'Bidder Demo', 'BIDDER', 'bidder01@example.com', '0912345678', '1995-03-03');

-- Categories are required before creating seller items because auction_items.category_id is a foreign key.
INSERT IGNORE INTO categories (category_id, category_name, slug, description, sort_order)
VALUES
    (1, 'Electronics', 'electronics', 'Electronic devices and accessories', 1),
    (2, 'Art', 'art', 'Artworks and collectibles', 2),
    (3, 'Vehicle', 'vehicle', 'Vehicles and transportation items', 3);

INSERT INTO auction_items (seller_id, category_id, item_name, description)
VALUES
    (2, 1, 'iPhone 13 Pro Max', 'Demo Electronics item: good condition phone'),
    (2, 2, 'Tranh sơn mài thủ công', 'Demo Art item: handmade lacquer painting'),
    (2, 3, 'Honda Wave Alpha', 'Demo Vehicle item: used motorbike in working condition');

-- Use relative dates so demo auctions remain biddable whenever the seed file is run.
INSERT INTO auction_sessions (
    item_id,
    starting_price,
    current_price,
    start_time,
    end_time,
    status
)
VALUES
    (1, 5000000, 5000000, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE'),
    (2, 12000000, 12000000, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 'SCHEDULED'),
    (3, 3000000, 3500000, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 5 DAY), 'ACTIVE');

INSERT INTO bids (auction_id, bidder_id, bid_amount, bid_time)
VALUES
    (3, 3, 3500000, NOW());
