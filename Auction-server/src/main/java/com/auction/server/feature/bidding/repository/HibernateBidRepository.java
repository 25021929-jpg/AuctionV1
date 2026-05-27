package com.auction.server.feature.bidding.repository;

import com.auction.server.entity.Bid;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class HibernateBidRepository implements BidRepository {

    private final SessionFactory sessionFactory;

    public HibernateBidRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Bid> findTopByAuction(Long auctionId, int limit) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Bid b
                        JOIN FETCH b.bidder
                        WHERE b.auctionSession.auctionId = :auctionId
                        ORDER BY b.bidTime DESC
                        """, Bid.class)
                .setParameter("auctionId", auctionId)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public Optional<Bid> findWinningBid(Long auctionId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Bid b
                        JOIN FETCH b.bidder
                        WHERE b.auctionSession.auctionId = :auctionId
                          AND b.isWinning = true
                        """, Bid.class)
                .setParameter("auctionId", auctionId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Bid> findByBidder(Long bidderId, int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Bid b
                        JOIN FETCH b.auctionSession a
                        JOIN FETCH a.item
                        WHERE b.bidder.id = :bidderId
                        ORDER BY b.bidTime DESC
                        """, Bid.class)
                .setParameter("bidderId", bidderId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public boolean existsByAuctionAndBidder(Long auctionId, Long bidderId) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        SELECT COUNT(b) FROM Bid b
                        WHERE b.auctionSession.auctionId = :auctionId
                          AND b.bidder.id = :bidderId
                        """, Long.class)
                .setParameter("auctionId", auctionId)
                .setParameter("bidderId", bidderId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public long countByAuction(Long auctionId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT COUNT(b) FROM Bid b " +
                                "WHERE b.auctionSession.auctionId = :auctionId",
                        Long.class)
                .setParameter("auctionId", auctionId)
                .getSingleResult();
    }

    @Override
    public Optional<BigDecimal> findMaxAmount(Long auctionId) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .createQuery(
                                "SELECT MAX(b.bidAmount) FROM Bid b " +
                                        "WHERE b.auctionSession.auctionId = :auctionId",
                                BigDecimal.class)
                        .setParameter("auctionId", auctionId)
                        .getSingleResult()
        );
    }

    @Override
    public Bid save(Bid bid) {
        // persist() cho object mới (id = null)
        // Nếu Service đang trong DbExecutor.run() → ghi vào transaction đó
        sessionFactory.getCurrentSession().persist(bid);
        return bid;
    }

    @Override
    public void clearWinningBids(Long auctionId) {
        // HQL UPDATE — nhanh hơn load từng entity
        // Chạy trên getCurrentSession() → cùng transaction với lock
        sessionFactory.getCurrentSession()
                .createMutationQuery(
                        "UPDATE Bid b SET b.isWinning = false " +
                                "WHERE b.auctionSession.auctionId = :auctionId " +
                                "  AND b.isWinning = true"
                )
                .setParameter("auctionId", auctionId)
                .executeUpdate();
    }
}
