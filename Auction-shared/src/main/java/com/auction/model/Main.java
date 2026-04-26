package com.auction.model;

/**
 * Demo chạy thử toàn bộ Model + 3 Design Patterns
 */
public class Main {
    public static void main(String[] args) {

        System.out.println("========== KHỞI TẠO USER (FACTORY METHOD PATTERN) ==========\n");

        // ===== FACTORY METHOD PATTERN =====
        Admin admin     = UserFactory.createAdmin("admin01", "123456", "admin@auction.vn", "Nguyễn Quản Trị");
        Seller seller   = UserFactory.createSeller("seller01", "123456", "seller@gmail.com", "Trần Văn Bán", 4.8);
        Bidder bidder1  = UserFactory.createBidder("bidder01", "123456", "b1@gmail.com", "Lê Thị Đặt", 50_000_000);
        Bidder bidder2  = UserFactory.createBidder("bidder02", "123456", "b2@gmail.com", "Phạm Văn Mua", 80_000_000);

        System.out.println("\n========== THÔNG TIN USER ==========\n");
        System.out.println(admin.getUserInfo());
        System.out.println(seller.getUserInfo());
        System.out.println(bidder1.getUserInfo());
        System.out.println(bidder2.getUserInfo());

        System.out.println("\n========== TẠO PHIÊN ĐẤU GIÁ ==========");

        // Tạo phiên đấu giá cho sản phẩm
        AuctionSession session = new AuctionSession("iPhone 16 Pro Max 256GB", 20_000_000);

        // Seller đăng sản phẩm (STRATEGY PATTERN)
        session.listItem(seller, 20_000_000);

        // ===== OBSERVER PATTERN - Đăng ký theo dõi phiên đấu giá =====
        System.out.println("\n--- Đăng ký theo dõi phiên đấu giá ---");
        session.addObserver(admin);    // Admin luôn theo dõi
        session.addObserver(seller);   // Seller theo dõi sản phẩm của mình
        session.addObserver(bidder1);  // Bidder 1 theo dõi
        session.addObserver(bidder2);  // Bidder 2 theo dõi

        System.out.println("\n========== BẮT ĐẦU ĐẤU GIÁ ==========");

        // Bidder 1 đặt giá (STRATEGY + OBSERVER)
        System.out.println("\n--- Lượt 1 ---");
        session.placeBid(bidder1, 22_000_000);

        // Bidder 2 đặt giá cao hơn
        System.out.println("\n--- Lượt 2 ---");
        session.placeBid(bidder2, 25_000_000);

        // Bidder 1 đặt lại
        System.out.println("\n--- Lượt 3 ---");
        session.placeBid(bidder1, 28_000_000);

        // Thử đặt thấp hơn -> bị chặn
        System.out.println("\n--- Lượt 4 (thử đặt thấp hơn) ---");
        session.placeBid(bidder2, 20_000_000);

        // Kết thúc phiên đấu giá (OBSERVER notify tất cả)
        System.out.println("\n========== KẾT THÚC ==========");
        session.endAuction();

        System.out.println("\n========== THÔNG TIN SAU ĐẤU GIÁ ==========\n");
        System.out.println(admin.getUserInfo());
        System.out.println("Admin log count: " + admin.getActivityLog().size() + " sự kiện");
        System.out.println(seller.getUserInfo());
        System.out.println(bidder1.getUserInfo());
        System.out.println(bidder2.getUserInfo());

        System.out.println("\n========== ADMIN ACTIONS ==========\n");
        admin.approveAuction("MacBook Pro M4");
        admin.banUser("spammer99");

        System.out.println("\n========== STRATEGY DESCRIPTION ==========\n");
        System.out.println(bidder1.getStrategyDescription());
        System.out.println(seller.getStrategyDescription());
    }
}