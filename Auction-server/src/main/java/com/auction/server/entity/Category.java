package com.auction.server.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"children", "items"})
@EqualsAndHashCode(of = {"categoryId"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;
    // Dùng Integer (không phải Long) vì danh mục thường ít — INT đủ

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;
    // slug: url-friendly, ví dụ "dien-tu", "thoi-trang"

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // ===== SELF-REFERENCING — quan hệ cha/con trong cùng bảng =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    // parent_id có thể NULL → Category gốc không có cha
    // Không có nullable=false vì danh mục gốc có parent=null
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    // Danh sách danh mục con

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AuctionItem> items = new ArrayList<>();
}