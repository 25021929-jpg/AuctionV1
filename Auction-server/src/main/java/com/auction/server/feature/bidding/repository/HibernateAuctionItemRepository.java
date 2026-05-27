package com.auction.server.feature.bidding.repository;


import com.auction.server.entity.AuctionItem;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation dùng Hibernate cho AuctionItemRepository.
 *
 * Nguyên tắc nhất quán với toàn bộ Repository layer:
 *   - getCurrentSession(): lấy Thread-bound session
 *   - KHÔNG quản lý transaction — Service + DbExecutor lo
 *   - JOIN FETCH đúng chỗ — hỏi "Service cần quan hệ nào?"
 *   - Optional cho single result, List cho multiple
 *   - KHÔNG trả null
 */
public class HibernateAuctionItemRepository
        implements AuctionItemRepository {

    private final SessionFactory sessionFactory;

    public HibernateAuctionItemRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ================================================================
    // READ METHODS
    // ================================================================

    /**
     * Tìm item theo id — không JOIN FETCH vì không biết caller cần gì.
     * Nếu caller cần quan hệ → dùng findByIdWithDetails().
     */
    @Override
    public Optional<AuctionItem> findById(Long id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .get(AuctionItem.class, id)
        );
    }

    /**
     * Tìm item kèm đầy đủ thông tin cho trang detail.
     *
     * Tại sao JOIN FETCH i.seller và i.category?
     *   Trang detail cần: tên seller, tên danh mục để hiển thị.
     *   Không JOIN FETCH → 2 query thêm khi gọi getter → N+1.
     *
     * Tại sao LEFT JOIN FETCH i.images?
     *   LEFT JOIN vì item có thể chưa có ảnh nào (list rỗng).
     *   Nếu dùng JOIN thường → item không có ảnh bị loại khỏi kết quả.
     *
     * Tại sao LEFT JOIN FETCH i.auctionSession?
     *   Trang detail có thể cần biết phiên đấu giá hiện tại.
     *   LEFT JOIN vì item DRAFT chưa có phiên nào.
     */
    @Override
    public Optional<AuctionItem> findByIdWithDetails(Long id) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionItem i
                        JOIN FETCH i.seller
                        JOIN FETCH i.category
                        LEFT JOIN FETCH i.images
                        LEFT JOIN FETCH i.auctionSession
                        WHERE i.itemId = :id
                        """, AuctionItem.class)
                .setParameter("id", id)
                // getResultStream().findFirst() thay vì getSingleResult()
                // getSingleResult() throw exception khi không tìm thấy
                // findFirst() trả Optional.empty() → an toàn hơn
                .getResultStream()
                .findFirst();
    }

    /**
     * Danh sách item của seller theo status — trang quản lý.
     *
     * Tại sao JOIN FETCH i.category?
     *   Seller muốn thấy item thuộc danh mục nào.
     *   Không JOIN FETCH → N query thêm nếu hiển thị danh sách.
     *
     * Tại sao KHÔNG JOIN FETCH i.seller?
     *   Caller đã biết sellerId rồi — load lại seller thừa.
     *
     * Tại sao KHÔNG JOIN FETCH i.images?
     *   Danh sách chỉ cần thumbnail (1 ảnh) — load hết ảnh lãng phí.
     *   Caller tự load ảnh chính khi cần: findByIdWithDetails().
     */
    @Override
    public List<AuctionItem> findBySeller(Long sellerId,
                                          AuctionItem.ItemStatus status,
                                          int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionItem i
                        JOIN FETCH i.category
                        WHERE i.seller.id = :sellerId
                          AND i.status = :status
                        ORDER BY i.createdAt DESC
                        """, AuctionItem.class)
                .setParameter("sellerId", sellerId)
                .setParameter("status", status)
                .setFirstResult(page * size)   // OFFSET
                .setMaxResults(size)            // LIMIT
                .getResultList();
        // getResultList() trả List rỗng nếu không có kết quả
        // KHÔNG trả null — caller không cần check null
    }

    /**
     * Item theo danh mục — chỉ hiện APPROVED.
     *
     * Tại sao JOIN FETCH i.seller?
     *   Trang danh mục hiện tên người bán dưới mỗi sản phẩm.
     *
     * Tại sao lọc status = APPROVED?
     *   Chỉ item được duyệt mới hiện cho buyer.
     *   DRAFT, PENDING, REJECTED → ẩn.
     */
    @Override
    public List<AuctionItem> findByCategory(Integer categoryId,
                                            int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionItem i
                        JOIN FETCH i.seller
                        WHERE i.category.categoryId = :categoryId
                          AND i.status = :status
                        ORDER BY i.createdAt DESC
                        """, AuctionItem.class)
                .setParameter("categoryId", categoryId)
                .setParameter("status", AuctionItem.ItemStatus.APPROVED)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Tìm kiếm theo tên — không phân biệt hoa thường.
     *
     * Tại sao LOWER() cả hai phía?
     *   "iPhone" và "iphone" đều tìm thấy nhau.
     *   Nếu chỉ LOWER một phía → không khớp.
     *
     * Tại sao % ở hai đầu keyword?
     *   Tìm từ khoá ở BẤT KỲ vị trí nào trong tên.
     *   "phone" → tìm thấy "iPhone 15 Pro".
     *
     * Hạn chế: LIKE với % ở đầu không dùng được index.
     * Khi cần hiệu năng cao hơn → chuyển sang Elasticsearch.
     */
    @Override
    public List<AuctionItem> searchByName(String keyword,
                                          int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionItem i
                        JOIN FETCH i.seller
                        JOIN FETCH i.category
                        WHERE LOWER(i.itemName) LIKE LOWER(:keyword)
                          AND i.status = :status
                        ORDER BY i.createdAt DESC
                        """, AuctionItem.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setParameter("status", AuctionItem.ItemStatus.APPROVED)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Item chờ duyệt — admin xử lý.
     *
     * Tại sao JOIN FETCH i.seller?
     *   Admin cần biết ai đăng item này để liên lạc nếu cần.
     *
     * Tại sao không phân trang?
     *   Admin thường xử lý hết queue — phân trang làm phức tạp thêm.
     *   Nếu queue lớn → thêm phân trang sau.
     */
    @Override
    public List<AuctionItem> findPendingReview() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionItem i
                        JOIN FETCH i.seller
                        JOIN FETCH i.category
                        WHERE i.status = :status
                        ORDER BY i.createdAt ASC
                        """, AuctionItem.class)
                // ASC: item đăng lâu nhất lên đầu — duyệt theo thứ tự
                .setParameter("status", AuctionItem.ItemStatus.PENDING_REVIEW)
                .getResultList();
    }

    // ================================================================
    // WRITE METHODS
    // ================================================================

    /**
     * Lưu item — ghi vào Thread-bound session hiện tại.
     * Nếu Service đang trong DbExecutor.run() → ghi vào transaction đó.
     * Nếu không → Hibernate auto-commit từng lệnh.
     */
    @Override
    public AuctionItem save(AuctionItem item) {
        return (AuctionItem) sessionFactory.getCurrentSession()
                .merge(item);
        // merge(): INSERT nếu id = null, UPDATE nếu đã có id
        // Dirty Checking: nếu entity đang Persistent → tự UPDATE khi flush
    }

    /**
     * Trả về Proxy object — không hit DB.
     *
     * Tại sao cần getReference()?
     *   Khi tạo AuctionSession cần set item,
     *   nhưng không cần load toàn bộ AuctionItem về RAM.
     *   getReference() tạo proxy chỉ có id → Hibernate dùng để set FK.
     *   Tiết kiệm 1 query không cần thiết.
     *
     * Khi nào proxy được load thật?
     *   Khi gọi bất kỳ getter nào ngoài getId() → Hibernate mới query DB.
     *   Nếu chỉ dùng để set FK → không bao giờ hit DB.
     */
    @Override
    public AuctionItem getReference(Long id) {
        return sessionFactory.getCurrentSession()
                .getReference(AuctionItem.class, id);
    }
}
