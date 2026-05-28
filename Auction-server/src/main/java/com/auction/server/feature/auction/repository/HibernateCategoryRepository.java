package com.auction.server.feature.auction.repository;


import com.auction.server.entity.Category;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation Hibernate cho CategoryRepository.
 *
 * Điểm đặc biệt: self-referencing relationship.
 * Category JOIN FETCH Category.children — join bảng với chính nó.
 */
public class HibernateCategoryRepository
        implements CategoryRepository {

    private final SessionFactory sessionFactory;

    public HibernateCategoryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession()
                        .get(Category.class, id)
        );
    }

    /**
     * Tìm theo slug — dùng cho URL-friendly navigation.
     * Slug là UNIQUE trong DB → tối đa 1 kết quả.
     */
    @Override
    public Optional<Category> findBySlug(String slug) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Category c WHERE c.slug = :slug",
                        Category.class)
                .setParameter("slug", slug)
                .getResultStream()
                .findFirst();
    }

    /**
     * Danh mục gốc — parent IS NULL.
     *
     * Tại sao ORDER BY c.sortOrder?
     *   Admin có thể sắp xếp thứ tự hiển thị danh mục.
     *   sortOrder = 0 mặc định, admin chỉnh lại theo ý muốn.
     *
     * Tại sao không JOIN FETCH c.children?
     *   findRootCategories() chỉ lấy level 1.
     *   Nếu cần cả children → dùng findAllWithChildren().
     */
    @Override
    public List<Category> findRootCategories() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Category c
                        WHERE c.parent IS NULL
                        ORDER BY c.sortOrder ASC, c.name ASC
                        """, Category.class)
                .getResultList();
    }

    /**
     * Danh mục con trực tiếp của một cha.
     *
     * Dùng khi: user click vào danh mục → load sub-category.
     * Lazy loading theo yêu cầu — không load hết cây ngay từ đầu.
     */
    @Override
    public List<Category> findChildren(Integer parentId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Category c
                        WHERE c.parent.categoryId = :parentId
                        ORDER BY c.sortOrder ASC, c.name ASC
                        """, Category.class)
                .setParameter("parentId", parentId)
                .getResultList();
    }

    /**
     * Toàn bộ cây danh mục — dùng cho menu navigation.
     *
     * Tại sao JOIN FETCH c.children?
     *   Render menu cần cả cha lẫn con trong 1 lần.
     *   Không JOIN FETCH → N+1: mỗi category gọi thêm query load children.
     *   10 category gốc × mỗi cái 5 con = 11 queries → JOIN FETCH = 1 query.
     *
     * Tại sao LEFT JOIN FETCH?
     *   LEFT JOIN: giữ lại category không có children (leaf node).
     *   JOIN thường: category không có con bị loại khỏi kết quả.
     *
     * Tại sao chỉ load 2 level (cha + con trực tiếp)?
     *   Menu thường chỉ hiện 2 level.
     *   Load sâu hơn → phức tạp, dùng Recursive CTE trong SQL thuần.
     */
    @Override
    public List<Category> findAllWithChildren() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                        FROM Category c
                        LEFT JOIN FETCH c.children
                        WHERE c.parent IS NULL
                        ORDER BY c.sortOrder ASC
                        """, Category.class)
                // DISTINCT tránh duplicate: Hibernate có thể trả về
                // cùng 1 parent nhiều lần nếu có nhiều con
                .setHint("hibernate.query.passDistinctThrough", false)
                .getResultList()
                .stream()
                .distinct()
                .toList();
    }

    @Override
    public Category save(Category category) {
        return (Category) sessionFactory.getCurrentSession()
                .merge(category);
    }

    @Override
    public void ensureDefaultCategories() {
        /*
         * Seller create/update needs auction_items.category_id to reference a real row.
         * Existing developer databases may have the schema but no seed data, so the
         * seller screen would fail with "Category not found: 1". INSERT IGNORE keeps
         * this idempotent: it creates the defaults once and does nothing afterward.
         */
        sessionFactory.getCurrentSession()
                .createNativeMutationQuery("""
                        INSERT IGNORE INTO categories
                            (category_id, category_name, slug, description, sort_order)
                        VALUES
                            (1, 'Phone', 'phone', 'Mobile phones and accessories', 1),
                            (2, 'Laptop', 'laptop', 'Laptops and computers', 2),
                            (3, 'Watch', 'watch', 'Smart watches and mechanical watches', 3),
                            (4, 'Motorbike', 'motorbike', 'Used motorbikes', 4)
                        """)
                .executeUpdate();
    }

    @Override
    public Category getReference(Integer id) {
        return sessionFactory.getCurrentSession()
                .getReference(Category.class, id);
    }
}
