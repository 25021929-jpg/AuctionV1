USE auction_db;

-- One-time migration for old development databases that still contain
-- Phone/Laptop/Watch/Motorbike. The canonical item types for this project are
-- defined in shared/domain/ItemType.java: Electronics, Art, Vehicle.
-- Run this after schema.sql if your ComboBox still shows the old categories.

START TRANSACTION;

INSERT INTO categories (category_name, slug, description, sort_order)
VALUES
    ('Electronics', 'electronics', 'Electronic devices and accessories', 1),
    ('Art', 'art', 'Artworks and collectibles', 2),
    ('Vehicle', 'vehicle', 'Vehicles and transportation items', 3)
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    description = VALUES(description),
    sort_order = VALUES(sort_order);

SET @electronics_id := (SELECT category_id FROM categories WHERE slug = 'electronics' LIMIT 1);
SET @vehicle_id := (SELECT category_id FROM categories WHERE slug = 'vehicle' LIMIT 1);

-- Old detailed technology categories are folded into the higher-level Electronics type.
UPDATE auction_items ai
JOIN categories c ON ai.category_id = c.category_id
SET ai.category_id = @electronics_id
WHERE LOWER(c.slug) IN ('phone', 'laptop', 'watch');

-- Old motorbike category is folded into Vehicle.
UPDATE auction_items ai
JOIN categories c ON ai.category_id = c.category_id
SET ai.category_id = @vehicle_id
WHERE LOWER(c.slug) IN ('motorbike');

-- Remove old category rows after dependent items have been moved.
DELETE FROM categories
WHERE LOWER(slug) IN ('phone', 'laptop', 'watch', 'motorbike');

COMMIT;

SELECT category_id, category_name, slug, sort_order
FROM categories
ORDER BY sort_order, category_name;
