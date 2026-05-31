package com.auction.server.feature.wallet.repository;

import com.auction.server.entity.WalletTransaction;
import java.util.List;
import org.hibernate.SessionFactory;

public class HibernateWalletTransactionRepository implements WalletTransactionRepository {
  private final SessionFactory sessionFactory;

  public HibernateWalletTransactionRepository(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public WalletTransaction save(WalletTransaction transaction) {
    return (WalletTransaction) sessionFactory.getCurrentSession().merge(transaction);
  }

  @Override
  public List<WalletTransaction> findByUser(Long userId, int limit) {
    return sessionFactory
        .getCurrentSession()
        .createQuery(
            """
                        FROM WalletTransaction wt
                        LEFT JOIN FETCH wt.auctionSession a
                        WHERE wt.user.id = :userId
                        ORDER BY wt.createdAt DESC
                        """,
            WalletTransaction.class)
        .setParameter("userId", userId)
        .setMaxResults(limit)
        .getResultList();
  }
}
