package com.auction.server.feature.bidding.repository;

import com.auction.server.entity.User;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class HibernateUserRepository implements UserRepository {

    private final SessionFactory sessionFactory;

    public HibernateUserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(User.class, id)
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                        Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<User> findByRole(User.Role role, int page, int size) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM User u WHERE u.role = :role AND u.isActive = true " +
                                "ORDER BY u.createdAt DESC",
                        User.class)
                .setParameter("role", role)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public User save(User user) {
        return (User) sessionFactory.getCurrentSession().merge(user);
    }

    @Override
    public boolean updateBalance(Long userId, BigDecimal newBalance) {
        int rows = sessionFactory.getCurrentSession()
                .createMutationQuery(
                        "UPDATE User u SET u.balance = :balance " +
                                "WHERE u.id = :id AND u.balance >= 0"
                )
                .setParameter("balance", newBalance)
                .setParameter("id", userId)
                .executeUpdate();
        return rows > 0;
    }

    @Override
    public User getReference(Long id) {
        // Proxy object — không hit DB, chỉ tạo placeholder với id
        // Hibernate dùng khi set FK mà không cần load toàn bộ entity
        return sessionFactory.getCurrentSession()
                .getReference(User.class, id);
    }

}
