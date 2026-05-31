package com.auction.server;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.database.DbExecutor;
import com.auction.server.database.HibernateUtil;
import com.auction.server.feature.auction.controller.AuctionController;
import com.auction.server.feature.auction.repository.HibernateAuctionItemRepository;
import com.auction.server.feature.auction.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.auction.repository.HibernateCategoryRepository;
import com.auction.server.feature.auction.scheduler.AuctionStatusScheduler;
import com.auction.server.feature.auction.service.AuctionService;
import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auth.repository.HibernateUserRepository;
import com.auction.server.feature.auth.service.AuthService;
import com.auction.server.feature.bidding.controller.BidController;
import com.auction.server.feature.bidding.repository.HibernateBidRepository;
import com.auction.server.feature.bidding.repository.HibernatePaymentRepository;
import com.auction.server.feature.bidding.service.BidService;
import com.auction.server.feature.seller.controller.SellerController;
import com.auction.server.feature.seller.service.SellerService;
import com.auction.server.feature.wallet.controller.WalletController;
import com.auction.server.feature.wallet.repository.HibernateWalletTransactionRepository;
import com.auction.server.feature.wallet.service.WalletService;
import com.auction.server.network.RequestDispatcher; // THÊM IMPORT
import com.auction.server.network.ServerSocketManager;
import org.hibernate.SessionFactory;

/**
 * Entry point for Auction Server. Starts the ServerSocketManager on the configured port (default
 * 8888).
 */
public class MainServer {

  public static void main(String[] args) {
    // Bước 1: Pool DB phải có trước khi ClientHandler / repository xử lý request
    DatabaseConnection.initializePool();

    // Bước 2: Hibernate sau — truyền pool vào
    HibernateUtil.initialize(DatabaseConnection.getDataSource());
    Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::shutdownPool));

    // =========================================================================
    // KIẾN TRÚC DI (Dependency Injection):
    // Khởi tạo duy nhất MỘT RequestDispatcher dùng chung cho toàn bộ Server tại đây.
    // Điều này ngăn chặn việc mỗi Client kết nối vào lại tự ý 'new RequestDispatcher()'
    // gây lãng phí tài nguyên RAM và lặp đi lặp lại việc khởi tạo Controller/Repository.
    // =========================================================================

    // Trích xuất SessionFactory chung để phân phát cho tất cả các tầng Repository
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    DbExecutor.init(sessionFactory);

    // =========================================================================
    // KIẾN TRÚC LẮP RÁP HỆ THỐNG TẬP TRUNG (Manual Dependency Injection Chain)
    // Lợi ích: Đảm bảo mỗi Class nghiệp vụ chỉ tồn tại DUY NHẤT 1 ĐỐI TƯỢNG (Singleton)
    // trên bộ nhớ Heap suốt vòng đời ứng dụng, ngăn chặn việc cấp phát RAM bừa bãi.
    // =========================================================================

    // BƯỚC 1: Khởi tạo tầng nền tảng hạ tầng - REPOSITORIES
    HibernateAuctionSessionRepository auctionSessionRepo =
        new HibernateAuctionSessionRepository(sessionFactory);
    HibernateAuctionItemRepository auctionItemRepo =
        new HibernateAuctionItemRepository(sessionFactory);
    HibernateCategoryRepository categoryRepo = new HibernateCategoryRepository(sessionFactory);
    HibernateUserRepository userRepo = new HibernateUserRepository(sessionFactory);
    HibernateBidRepository bidRepo = new HibernateBidRepository(sessionFactory);
    HibernatePaymentRepository paymentRepo = new HibernatePaymentRepository(sessionFactory);
    HibernateWalletTransactionRepository walletTransactionRepo =
        new HibernateWalletTransactionRepository(sessionFactory);

    // BƯỚC 2: Khởi tạo tầng lõi xử lý nghiệp vụ - SERVICES (Bơm các Repository tương ứng vào)
    AuctionService auctionService =
        new AuctionService(auctionSessionRepo, auctionItemRepo, categoryRepo, userRepo);
    BidService bidService =
        new BidService(auctionSessionRepo, bidRepo, paymentRepo, userRepo, walletTransactionRepo);
    AuthService authService =
        new AuthService(userRepo); // Đảm bảo lớp AuthService của bạn cũng dùng Constructor DI
    SellerService sellerService =
        new SellerService(auctionSessionRepo, auctionItemRepo, categoryRepo, userRepo);
    WalletService walletService = new WalletService(userRepo, walletTransactionRepo);

    // BƯỚC 3: Khởi tạo tầng giao tiếp API - CONTROLLERS (Bơm các Service tương ứng vào)
    AuctionController auctionController = new AuctionController(auctionService);
    BidController bidController = new BidController(bidService);
    AuthController authController = new AuthController(authService);
    SellerController sellerController = new SellerController(sellerService);
    WalletController walletController = new WalletController(walletService);

    // BƯỚC 4: Khởi tạo cổng điều phối mạng trung tâm - DISPATCHER
    RequestDispatcher dispatcher =
        new RequestDispatcher(
            authController, auctionController, bidController, sellerController, walletController);

    // BƯỚC 5: Scheduler realtime cập nhật trạng thái SCHEDULED -> ACTIVE -> ENDED mỗi giây.
    AuctionStatusScheduler statusScheduler =
        new AuctionStatusScheduler(
            auctionSessionRepo, paymentRepo, bidRepo, userRepo, walletTransactionRepo);
    statusScheduler.start();
    Runtime.getRuntime().addShutdownHook(new Thread(statusScheduler::stop));

    // =========================================================================
    // KHỞI CHẠY TẦNG MẠNG SOCKET
    // =========================================================================
    int port = 8888;
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Invalid port argument, using default 8888");
      }
    }
    System.out.println("Starting Auction Server on port " + port);

    // Truyền dispatcher tập trung vào bộ quản lý Socket
    ServerSocketManager server = new ServerSocketManager(port, dispatcher);
    server.start();
  }
}
