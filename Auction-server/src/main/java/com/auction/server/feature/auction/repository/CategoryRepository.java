package com.auction.server.feature.auction.repository;

import com.auction.server.entity.Category;
import java.util.List;
import java.util.Optional;

/**
 * Contract cho bảng categories.
 *
 * Category có tính chất đặc biệt: self-referencing (cha-con).
 * Một category có thể có parent (danh mục cha)
 * và danh sách children (danh mục con).
 *
 * Ví dụ:
 *   Điện tử (gốc)
 *     └── Điện thoại (con)
 *           └── iPhone (cháu)
 */
public interface CategoryRepository {

    Optional<Category> findById(Integer id);

    /** Tìm theo slug — dùng cho URL: /category/dien-thoai */
    Optional<Category> findBySlug(String slug);

    /** Lấy tất cả danh mục gốc (parent = null) */
    List<Category> findRootCategories();

    /**
     * Lấy danh mục con trực tiếp của một cha.
     * Dùng cho: render menu dropdown.
     */
    List<Category> findChildren(Integer parentId);

    /**
     * Lấy toàn bộ cây danh mục — dùng cho menu navigation.
     * Load tất cả trong 1 query, tránh N+1 khi render cây.
     */
    List<Category> findAllWithChildren();

    Category save(Category category);

    /** Proxy — không hit DB, dùng để set FK */
    Category getReference(Integer id);
}