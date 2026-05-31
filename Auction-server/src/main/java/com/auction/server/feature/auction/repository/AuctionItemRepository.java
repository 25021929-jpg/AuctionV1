package com.auction.server.feature.auction.repository;

import com.auction.server.entity.AuctionItem;
import java.util.List;
import java.util.Optional;

/**
 * Contract cho mọi thao tác DB liên quan đến bảng auction_items.
 *
 * <p>Service chỉ biết interface này. Mọi chi tiết Hibernate, Session, SQL đều ẩn trong
 * Implementation.
 */
public interface AuctionItemRepository {

  // ===== READ =====

  /** Tìm item theo id — trả Optional vì có thể không tồn tại */
  Optional<AuctionItem> findById(Long id);

  /** Tìm item kèm ảnh + seller + category. Dùng cho: trang detail item, admin duyệt item. */
  Optional<AuctionItem> findByIdWithDetails(Long id);

  /**
   * Danh sách item của một seller, lọc theo status, phân trang. Dùng cho: trang quản lý của người
   * bán.
   */
  List<AuctionItem> findBySeller(Long sellerId, AuctionItem.ItemStatus status, int page, int size);

  /** Danh sách item theo danh mục, chỉ APPROVED. Dùng cho: trang danh mục sản phẩm. */
  List<AuctionItem> findByCategory(Integer categoryId, int page, int size);

  /** Tìm kiếm item theo tên — không phân biệt hoa thường. Dùng cho: thanh search. */
  List<AuctionItem> searchByName(String keyword, int page, int size);

  /** Danh sách item chờ duyệt — admin xử lý. Không cần phân trang vì admin thường xử lý hết. */
  List<AuctionItem> findPendingReview();

  // ===== WRITE =====

  /** Lưu item mới hoặc cập nhật */
  AuctionItem save(AuctionItem item);

  /** Lấy proxy của item — không hit DB. Dùng khi cần set FK mà không cần load toàn bộ item. */
  AuctionItem getReference(Long id);
}
