package com.auction.server.feature.auction.repository;

import com.auction.server.entity.AuctionSession;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation dùng Hibernate.
 * Repository chỉ lo query — không quản lý transaction.
 * Session lấy qua getCurrentSession() — Session của Thread hiện tại.
 * Lock được thiết lập trên Session đó — sống theo transaction của Service.
 */
public class HibernateAuctionSessionRepository
        implements AuctionSessionRepository {

    private final SessionFactory sessionFactory;

    // Inject SessionFactory qua constructor — không dùng static
    // → dễ test, dễ thay thế
    public HibernateAuctionSessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ================================================================
    // READ — không transaction, session đọc snapshot qua MVCC
    // ================================================================

    @Override
    public Optional<AuctionSession> findById(Long id) {
        // getCurrentSession(): lấy session đang bind với Thread này
        // Nếu Service đã mở transaction → dùng session đó
        // Nếu chưa có → tạo session mới (read-only context)
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .get(AuctionSession.class, id)
        );
    }

    @Override
    public Optional<AuctionSession> findByIdWithDetails(Long id) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        JOIN FETCH a.item i
                        JOIN FETCH i.seller
                        JOIN FETCH i.category
                        LEFT JOIN FETCH i.images
                        LEFT JOIN FETCH a.winner
                        WHERE a.auctionId = :id
                        """, AuctionSession.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<AuctionSession> findActive(int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        JOIN FETCH a.item i
                        JOIN FETCH i.seller
                        JOIN FETCH i.category
                        WHERE a.status = :status
                          AND a.endTime > :now
                        ORDER BY a.endTime ASC
                        """, AuctionSession.class)
                .setParameter("status", AuctionSession.AuctionStatus.ACTIVE)
                .setParameter("now", LocalDateTime.now())
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<AuctionSession> findExpired() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        WHERE a.status = :status
                          AND a.endTime <= :now
                        """, AuctionSession.class)
                .setParameter("status", AuctionSession.AuctionStatus.ACTIVE)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    @Override
    public List<AuctionSession> findEndedAwaitingSettlement() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        WHERE a.status = :status
                          AND a.winner IS NOT NULL
                          AND NOT EXISTS (
                              SELECT 1
                              FROM Payment p
                              WHERE p.auctionSession = a
                          )
                        """, AuctionSession.class)
                .setParameter("status", AuctionSession.AuctionStatus.ENDED)
                .getResultList();
    }

    @Override
    public List<AuctionSession> findScheduledToStart() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        WHERE a.status = :status
                          AND a.startTime <= :now
                        """, AuctionSession.class)
                .setParameter("status", AuctionSession.AuctionStatus.SCHEDULED)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    @Override
    public List<AuctionSession> findBySeller(Long sellerId,
                                             int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM AuctionSession a
                        JOIN FETCH a.item i
                        WHERE i.seller.id = :sellerId
                        ORDER BY a.createdAt DESC
                        """, AuctionSession.class)
                .setParameter("sellerId", sellerId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // ================================================================
    // READ WITH LOCK
    // Tại sao chữ ký sạch (không có Session tham số)?
    //
    // Service gọi DbExecutor.run() → DbExecutor mở transaction trên
    // Thread-bound session → bind session với Thread hiện tại.
    // Repository gọi getCurrentSession() → nhận đúng session đó.
    // Lock thiết lập trên session đó → sống trong transaction của Service.
    // Service commit → lock giải phóng.
    //
    // Toàn bộ diễn ra ngầm — Repository không biết, Service không biết Session.
    // ================================================================

    @Override
    public Optional<AuctionSession> findByIdWithLock(Long id) {
        // getCurrentSession() trả về session đang có transaction của Service
        // PESSIMISTIC_WRITE → SELECT FOR UPDATE trên session đó
        // Lock tồn tại cho đến khi Service commit (trong DbExecutor.run())
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .get(
                                AuctionSession.class,
                                id,
                                LockMode.PESSIMISTIC_WRITE
                        )
        );
    }

    // ================================================================
    // WRITE
    // Repository không gọi begin/commit/rollback
    // Nếu Service đang trong transaction (DbExecutor.run()) → ghi vào đó
    // Nếu không → Hibernate tự-commit từng lệnh (auto-commit mode)
    // ================================================================

    @Override
    public AuctionSession save(AuctionSession auction) {
        return (AuctionSession) sessionFactory.getCurrentSession()
                .merge(auction);
        // merge(): INSERT nếu mới, UPDATE nếu đã có id
        // Dirty Checking: nếu entity đang Persistent → tự UPDATE khi flush
    }

    @Override
    public int bulkUpdateStatus(List<Long> ids,
                                AuctionSession.AuctionStatus status) {
        if (ids == null || ids.isEmpty()) return 0;
        return sessionFactory.getCurrentSession()
                .createMutationQuery(
                        "UPDATE AuctionSession a SET a.status = :status " +
                                "WHERE a.auctionId IN :ids"
                )
                .setParameter("status", status)
                .setParameter("ids", ids)
                .executeUpdate();
    }
}
