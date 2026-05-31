package com.auction.client.core.ui;

/**
 * Gom đường dẫn FXML về 1 chỗ để tránh hardcode rải rác. Điều này giúp hạn chế sửa code cũ bừa bãi
 * khi đổi cấu trúc view.
 */
public final class ScenePaths {

  private ScenePaths() {}

  // Auth
  public static final String LOGIN = "/com/auction/client/feature/auth/view/login-view.fxml";
  public static final String REGISTER = "/com/auction/client/feature/auth/view/register-view.fxml";
  public static final String HOME = "/com/auction/client/feature/auth/view/home-view.fxml";

  // Auction
  public static final String AUCTION_LIST =
      "/com/auction/client/feature/auction/view/auction-list-view.fxml";
  public static final String AUCTION_DETAIL =
      "/com/auction/client/feature/auction/view/auction-detail-view.fxml";
  public static final String LIVE_BIDDING =
      "/com/auction/client/feature/bidding/view/live-bidding-view.fxml";

  // Seller
  public static final String SELLER_DASHBOARD =
      "/com/auction/client/feature/seller/view/seller-dashboard-view.fxml";

  // Admin
  public static final String ADMIN_DASHBOARD =
      "/com/auction/client/feature/admin/view/admin-dashboard-view.fxml";
}
