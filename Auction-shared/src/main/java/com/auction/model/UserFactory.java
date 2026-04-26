package com.auction.model;

/**
 * FACTORY METHOD PATTERN
 * Tạo các loại User tương ứng theo Role mà không cần biết class cụ thể
 *
 * Lợi ích:
 * - Không cần new Admin() / new Bidder() / new Seller() rải rác trong code
 * - Dễ dàng mở rộng thêm loại User mới mà không sửa code cũ
 * - Tập trung logic khởi tạo User vào một nơi duy nhất
 */
public class UserFactory {

    private static int idCounter = 1; // Auto-increment ID

    /**
     * Tạo Admin
     */
    public static Admin createAdmin(String username, String password,
                                    String email, String fullName) {
        Admin admin = new Admin(idCounter++, username, password, email, fullName);
        System.out.println("[FACTORY] Tạo Admin: " + username);
        return admin;
    }

    /**
     * Tạo Bidder với số dư ban đầu
     */
    public static Bidder createBidder(String username, String password,
                                      String email, String fullName, double balance) {
        Bidder bidder = new Bidder(idCounter++, username, password, email, fullName, balance);
        System.out.println("[FACTORY] Tạo Bidder: " + username + " | Số dư: " + balance + " VNĐ");
        return bidder;
    }

    /**
     * Tạo Seller với rating ban đầu
     */
    public static Seller createSeller(String username, String password,
                                      String email, String fullName, double rating) {
        Seller seller = new Seller(idCounter++, username, password, email, fullName, rating);
        System.out.println("[FACTORY] Tạo Seller: " + username + " | Rating: " + rating);
        return seller;
    }

    /**
     * Tạo User theo Role - dùng khi chỉ biết Role (dùng default values)
     */
    public static User createUser(Role role, String username, String password,
                                  String email, String fullName) {
        switch (role) {
            case ADMIN:
                return createAdmin(username, password, email, fullName);
            case BIDDER:
                return createBidder(username, password, email, fullName, 0.0);
            case SELLER:
                return createSeller(username, password, email, fullName, 5.0);
            default:
                throw new IllegalArgumentException("Role không hợp lệ: " + role);
        }
    }

    /**
     * Reset ID counter (dùng cho testing)
     */
    public static void resetIdCounter() {
        idCounter = 1;
    }
}