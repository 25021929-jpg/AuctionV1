package com.auction.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter                             // Lombok tự tạo getter cho mọi field
@Setter                             // Lombok tự tạo setter cho mọi field
@NoArgsConstructor                  // Constructor không tham số — JPA BẮT BUỘC phải có
@AllArgsConstructor                 // Constructor đủ tất cả tham số
@Builder                            // User.builder().username("a").build()
@ToString(exclude = {"items", "bids", "payments"})
// exclude: tránh StackOverflow khi print — vì items lại chứa User, vòng lặp vô tận
@EqualsAndHashCode(of = {"id"})
// So sánh User chỉ dựa vào id, không dựa vào tất cả field
// Quan trọng khi dùng trong Set hoặc so sánh entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = dùng AUTO_INCREMENT của MySQL
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    // Lưu ý: tên field là passwordHash (camelCase)
    // map với cột password_hash (snake_case) trong DB

    @Enumerated(EnumType.STRING)
    // STRING: lưu "ADMIN", "SELLER", "BUYER" — không lưu 0, 1, 2
    // Nếu dùng ORDINAL: thêm MODERATOR vào giữa → số thứ tự thay đổi → dữ liệu cũ sai
    @Column(name = "role", nullable = false, length = 10)
    @Builder.Default                // khi dùng @Builder, cần @Builder.Default để giá trị mặc định hoạt động
    private Role role = Role.BUYER;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    // precision=15: tổng 15 chữ số
    // scale=2: 2 chữ số thập phân
    // → tối đa 9,999,999,999,999.99

    @Column(name = "phone", length = 20)
    private String phone;           // nullable — không bắt buộc

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;  // LocalDate: chỉ ngày, không giờ phút giây

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    // Boolean (wrapper) thay vì boolean (primitive)
    // → cho phép null, Hibernate map TINYINT(1) ↔ Boolean tự động

    @CreationTimestamp
    // Hibernate tự set = NOW() khi INSERT
    // Bạn không cần set thủ công trong code
    @Column(name = "created_at", nullable = false, updatable = false)
    // updatable=false: cột này không bao giờ bị UPDATE sau khi INSERT
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Hibernate tự cập nhật = NOW() mỗi khi object thay đổi
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===== QUAN HỆ =====

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    // mappedBy = "seller": tên field bên AuctionItem trỏ về User
    // LAZY: không load list này cho đến khi gọi getItems()
    // @OneToMany không có @JoinColumn vì User không giữ FK
    @Builder.Default
    private List<AuctionItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "bidder", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    // Enum định nghĩa ngay trong class — gọn hơn tạo file riêng
    public enum Role {
        ADMIN, SELLER, BUYER
    }
}