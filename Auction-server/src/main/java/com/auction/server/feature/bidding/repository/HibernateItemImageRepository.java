package com.auction.server.feature.bidding.repository;


import com.auction.server.entity.ItemImage;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation Hibernate cho ItemImageRepository.
 *
 * Lưu ý quan trọng về Cascade:
 *   AuctionItem có cascade = ALL đến ItemImage.
 *   → save(item) tự save images.
 *   → delete(item) tự delete images.
 *   Repository này chỉ cần thiết khi thao tác ảnh ĐỘC LẬP
 *   (thêm/xoá ảnh sau khi item đã tạo).
 */
public class HibernateItemImageRepository
        implements ItemImageRepository {

    private final SessionFactory sessionFactory;

    public HibernateItemImageRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Tất cả ảnh của item — sắp xếp theo sort_order.
     * sort_order = 0 (ảnh chính thường là 0) lên đầu.
     */
    @Override
    public List<ItemImage> findByItem(Long itemId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM ItemImage img
                        WHERE img.item.itemId = :itemId
                        ORDER BY img.isPrimary DESC, img.sortOrder ASC
                        """, ItemImage.class)
                // isPrimary DESC: ảnh chính (true=1) lên đầu
                // sortOrder ASC: các ảnh còn lại theo thứ tự
                .setParameter("itemId", itemId)
                .getResultList();
    }

    /**
     * Ảnh chính của item — dùng cho thumbnail trong danh sách.
     *
     * Trả Optional vì item có thể chưa có ảnh nào.
     */
    @Override
    public Optional<ItemImage> findPrimaryImage(Long itemId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM ItemImage img
                        WHERE img.item.itemId = :itemId
                          AND img.isPrimary = true
                        """, ItemImage.class)
                .setParameter("itemId", itemId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public ItemImage save(ItemImage image) {
        return (ItemImage) sessionFactory.getCurrentSession()
                .merge(image);
    }

    /**
     * Xoá ảnh theo id.
     *
     * Tại sao load entity trước rồi mới remove?
     * session.remove() cần Persistent entity — không dùng được id trực tiếp.
     * Nếu không tìm thấy → không làm gì (idempotent).
     */
    @Override
    public void deleteById(Long imageId) {
        sessionFactory.getCurrentSession()
                .createMutationQuery(
                        "DELETE FROM ItemImage img WHERE img.id = :id")
                .setParameter("id", imageId)
                .executeUpdate();
        // HQL DELETE thẳng — không cần load entity
        // Nhanh hơn: không tốn 1 SELECT trước khi DELETE
    }

    /**
     * Đặt ảnh làm ảnh chính — 2 bước trong cùng transaction.
     *
     * Tại sao phải 2 bước?
     *   Bảng có thể có nhiều ảnh is_primary = true nếu không reset.
     *   Bước 1 reset tất cả → Bước 2 set đúng 1 ảnh.
     *   2 bước này PHẢI trong cùng 1 transaction của Service.
     *   Nếu bước 2 lỗi → bước 1 rollback → không có ảnh nào là primary.
     *
     * Tại sao không trả boolean?
     *   Nếu imageId không tồn tại → bước 2 update 0 row.
     *   Service tự kiểm tra bằng findPrimaryImage() sau đó nếu cần.
     */
    @Override
    public void setPrimaryImage(Long itemId, Long imageId) {
        // Bước 1: reset tất cả ảnh của item
        sessionFactory.getCurrentSession()
                .createMutationQuery(
                        """
                        UPDATE ItemImage img
                        SET img.isPrimary = false
                        WHERE img.item.itemId = :itemId
                        """)
                .setParameter("itemId", itemId)
                .executeUpdate();

        // Bước 2: set ảnh được chọn làm primary
        sessionFactory.getCurrentSession()
                .createMutationQuery(
                        """
                        UPDATE ItemImage img
                        SET img.isPrimary = true
                        WHERE img.id = :imageId
                          AND img.item.itemId = :itemId
                        """)
                // AND img.item.itemId = :itemId: đảm bảo ảnh thuộc đúng item
                // tránh set ảnh của item khác làm primary nhầm
                .setParameter("imageId", imageId)
                .setParameter("itemId", itemId)
                .executeUpdate();
    }
}
