package com.auction.server.feature.auction.repository;


import com.auction.server.entity.ItemImage;
import java.util.List;
import java.util.Optional;

/**
 * Contract cho bảng item_images.
 *
 * ItemImage là bảng phụ thuộc hoàn toàn vào AuctionItem.
 * Không có ý nghĩa khi đứng độc lập.
 * Cascade ALL từ AuctionItem → save/delete item tự lo ảnh.
 *
 * Repository này chủ yếu dùng khi cần thao tác ảnh riêng lẻ:
 * thêm ảnh, xoá ảnh cụ thể, đổi ảnh chính.
 */
public interface ItemImageRepository {

    /** Tất cả ảnh của một item — đã sort theo sort_order */
    List<ItemImage> findByItem(Long itemId);

    /** Ảnh chính (is_primary = true) của một item */
    Optional<ItemImage> findPrimaryImage(Long itemId);

    /** Lưu ảnh mới */
    ItemImage save(ItemImage image);

    /** Xoá ảnh theo id */
    void deleteById(Long imageId);

    /**
     * Đặt ảnh làm ảnh chính.
     * Bước 1: set is_primary = false cho tất cả ảnh của item.
     * Bước 2: set is_primary = true cho ảnh được chọn.
     */
    void setPrimaryImage(Long itemId, Long imageId);
}
