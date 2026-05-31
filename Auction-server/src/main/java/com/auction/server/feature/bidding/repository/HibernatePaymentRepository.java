package com.auction.server.feature.bidding.repository;


import com.auction.server.entity.Payment;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation dùng Hibernate cho PaymentRepository.
 *
 * Điểm đặc biệt của Payment so với Repository khác:
 *   - Dữ liệu nhạy cảm: liên quan tiền thật
 *   - updateStatus() dùng HQL UPDATE có điều kiện
 *     để tránh ghi đè trạng thái sai
 *   - sumNetRevenueBySeller() cần xử lý null từ SUM
 */
public class HibernatePaymentRepository
        implements PaymentRepository {

    private final SessionFactory sessionFactory;

    public HibernatePaymentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ================================================================
    // READ METHODS
    // ================================================================

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .get(Payment.class, id)
        );
    }

    /**
     * Tìm payment theo auction.
     *
     * Tại sao không JOIN FETCH?
     *   Caller thường chỉ cần kiểm tra payment có tồn tại không.
     *   Nếu cần chi tiết → dùng findByBuyer() hoặc findBySeller().
     */
    @Override
    public Optional<Payment> findByAuction(Long auctionId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Payment p WHERE p.auctionSession.auctionId = :id",
                        Payment.class)
                .setParameter("id", auctionId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Payment> findByAuctionWithDetails(Long auctionId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Payment p " +
                                "JOIN FETCH p.buyer b " +
                                "JOIN FETCH p.seller s " +
                                "JOIN FETCH p.auctionSession a " +
                                "JOIN FETCH a.item i " +
                                "WHERE a.auctionId = :id",
                        Payment.class)
                .setParameter("id", auctionId)
                .getResultStream()
                .findFirst();
    }

    /**
     * Lịch sử mua của buyer.
     *
     * Tại sao JOIN FETCH p.auctionSession a?
     *   Cần hiển thị thông tin phiên: thời gian, trạng thái.
     *
     * Tại sao JOIN FETCH a.item?
     *   Buyer muốn xem "mình đã mua mặt hàng gì".
     *   Không JOIN FETCH → N+1 khi render danh sách.
     *
     * Tại sao KHÔNG JOIN FETCH p.seller?
     *   Trang buyer không cần biết chi tiết seller.
     *   Chỉ cần tên — có thể lấy từ item.seller nếu cần.
     */
    @Override
    public List<Payment> findByBuyer(Long buyerId, int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Payment p
                        JOIN FETCH p.auctionSession a
                        JOIN FETCH a.item i
                        JOIN FETCH i.seller
                        WHERE p.buyer.id = :buyerId
                        ORDER BY p.createdAt DESC
                        """, Payment.class)
                .setParameter("buyerId", buyerId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lịch sử bán của seller — chỉ COMPLETED.
     *
     * Tại sao chỉ COMPLETED?
     *   Seller quan tâm đến doanh thu thật sự đã nhận.
     *   PENDING/FAILED → chưa có tiền thật → không cần hiển thị.
     *
     * Tại sao JOIN FETCH a.item?
     *   Seller muốn xem "mình đã bán mặt hàng gì, cho ai".
     *
     * Tại sao JOIN FETCH p.buyer?
     *   Seller muốn xem thông tin người mua.
     */
    @Override
    public List<Payment> findBySeller(Long sellerId, int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Payment p
                        JOIN FETCH p.auctionSession a
                        JOIN FETCH a.item
                        JOIN FETCH p.buyer
                        WHERE p.seller.id = :sellerId
                          AND p.status = :status
                        ORDER BY p.createdAt DESC
                        """, Payment.class)
                .setParameter("sellerId", sellerId)
                .setParameter("status", Payment.PaymentStatus.COMPLETED)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Payment PENDING quá lâu — cron job xử lý.
     *
     * Tại sao có threshold thay vì hardcode 24h?
     *   Linh hoạt: caller quyết định ngưỡng timeout.
     *   Ví dụ: threshold = LocalDateTime.now().minusHours(24)
     *   Dễ test: truyền threshold bất kỳ vào khi test.
     *
     * Tại sao không JOIN FETCH?
     *   Cron job chỉ cần update status — không cần hiển thị.
     *   Không JOIN → nhẹ hơn, nhanh hơn.
     */
    @Override
    public List<Payment> findPendingOlderThan(LocalDateTime threshold) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Payment p
                        WHERE p.status = :status
                          AND p.createdAt < :threshold
                        ORDER BY p.createdAt ASC
                        """, Payment.class)
                .setParameter("status", Payment.PaymentStatus.PENDING)
                .setParameter("threshold", threshold)
                .getResultList();
    }

    /**
     * Tổng doanh thu thực của seller.
     *
     * Tại sao (p.amount - p.platformFee)?
     *   Doanh thu thực = tiền nhận - phí sàn.
     *   amount: giá thắng auction.
     *   platformFee: phí sàn khấu trừ.
     *
     * Tại sao kiểm tra null trước khi trả về?
     *   SUM trả NULL khi không có dòng nào thỏa điều kiện.
     *   Ví dụ: seller mới, chưa có payment COMPLETED nào.
     *   Trả BigDecimal.ZERO thay vì null → caller không cần check null.
     */
    @Override
    public BigDecimal sumNetRevenueBySeller(Long sellerId) {
        BigDecimal result = sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        SELECT SUM(p.amount - p.platformFee)
                        FROM Payment p
                        WHERE p.seller.id = :sellerId
                          AND p.status = :status
                        """, BigDecimal.class)
                .setParameter("sellerId", sellerId)
                .setParameter("status", Payment.PaymentStatus.COMPLETED)
                .getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    // ================================================================
    // WRITE METHODS
    // ================================================================

    /**
     * Lưu payment — ghi vào Thread-bound session.
     * Service đang trong DbExecutor.run() → ghi vào transaction đó.
     */
    @Override
    public Payment save(Payment payment) {
        return (Payment) sessionFactory.getCurrentSession()
                .merge(payment);
    }

    /**
     * Cập nhật status payment — dùng khi cổng thanh toán callback.
     *
     * Tại sao dùng HQL UPDATE thay vì load entity rồi merge?
     *   Atomic: đọc + ghi trong 1 câu lệnh — an toàn hơn với dữ liệu tiền.
     *   Có điều kiện AND p.status = 'PENDING':
     *     Chỉ update nếu đang PENDING.
     *     Tránh ghi đè payment đã COMPLETED hoặc FAILED.
     *     Ví dụ: callback đến 2 lần → lần 2 không update → an toàn.
     *
     * Tại sao trả boolean?
     *   rowsAffected = 0: payment không tồn tại hoặc không còn PENDING.
     *   Service dựa vào kết quả này để quyết định xử lý tiếp.
     */
    @Override
    public boolean updateStatus(Long paymentId,
                                Payment.PaymentStatus newStatus,
                                String transactionRef) {
        int rows = sessionFactory.getCurrentSession()
                .createMutationQuery(
                        """
                        UPDATE Payment p
                        SET p.status = :newStatus,
                            p.transactionRef = :ref,
                            p.paidAt = :now
                        WHERE p.id = :id
                          AND p.status = :pendingStatus
                        """)
                // Chỉ update khi đang PENDING
                // Bảo vệ idempotency: gọi nhiều lần vẫn an toàn
                .setParameter("newStatus",     newStatus)
                .setParameter("ref",           transactionRef)
                .setParameter("now",           java.time.LocalDateTime.now())
                .setParameter("id",            paymentId)
                .setParameter("pendingStatus", Payment.PaymentStatus.PENDING)
                .executeUpdate();
        return rows > 0;
    }
}