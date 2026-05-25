package com.auction.server.entity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"auctionSession", "images"})
@EqualsAndHashCode(of = {"itemId"})
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    // ===== FK → User (seller) =====
    @ManyToOne(fetch = FetchType.LAZY)
    // FetchType.LAZY: không load User khi load AuctionItem
    // Chỉ load khi gọi item.getSeller()
    @JoinColumn(name = "seller_id", nullable = false)
    // JoinColumn: chỉ định tên cột FK trong bảng auction_items
    private User seller;

    // ===== FK → Category =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    // columnDefinition = "TEXT": override kiểu cột
    // Nếu không có → Hibernate dùng VARCHAR(255) mặc định → mô tả dài bị cắt
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 10)
    @Builder.Default
    private ItemCondition condition = ItemCondition.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ItemStatus status = ItemStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===== QUAN HỆ NGƯỢC =====

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY)
    // mappedBy = "item": field bên AuctionSession
    // AuctionSession giữ FK (item_id), nên AuctionItem không có @JoinColumn
    private AuctionSession auctionSession;

    @OneToMany(
            mappedBy = "item",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            // CascadeType.ALL: mọi thao tác trên AuctionItem đều lan sang ItemImage
            // save(item) → save(images) luôn
            // delete(item) → delete(images) luôn
            orphanRemoval = true
            // orphanRemoval: xoá image khỏi list → DELETE image đó trong DB
            // ví dụ: item.getImages().remove(img) → Hibernate tự DELETE img
    )
    @Builder.Default
    private List<ItemImage> images = new ArrayList<>();

    // Enum
    public enum ItemCondition { NEW, LIKE_NEW, GOOD, FAIR, POOR }
    public enum ItemStatus    { DRAFT, PENDING_REVIEW, APPROVED, REJECTED, ARCHIVED }
}