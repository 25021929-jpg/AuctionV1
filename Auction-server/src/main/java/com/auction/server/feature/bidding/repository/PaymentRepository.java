package com.auction.server.feature.bidding.repository;

import com.auction.server.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contract cho mọi thao tác DB liên quan đến bảng payments.
 *
 * <p>Payment là bảng nhạy cảm nhất — liên quan đến tiền thật. Mọi thao tác ghi phải nằm trong
 * transaction.
 */
public interface PaymentRepository {

  // ===== READ =====

  /** Tìm payment theo id */
  Optional<Payment> findById(Long id);

  /** Tìm payment của một phiên đấu giá. 1 auction = tối đa 1 payment (UNIQUE constraint). */
  Optional<Payment> findByAuction(Long auctionId);

  /**
   * Tìm payment của một phiên đấu giá và load đầy đủ quan hệ cần hiển thị (buyer, seller,
   * auctionSession, item). Dùng khi caller cần render chi tiết của payment trong 1 truy vấn duy
   * nhất để tránh N+1.
   */
  Optional<Payment> findByAuctionWithDetails(Long auctionId);

  /** Lịch sử mua hàng của buyer — phân trang. Dùng cho: trang "Đơn hàng của tôi". */
  List<Payment> findByBuyer(Long buyerId, int page, int size);

  /** Lịch sử bán hàng của seller — chỉ COMPLETED. Dùng cho: trang "Doanh thu của tôi". */
  List<Payment> findBySeller(Long sellerId, int page, int size);

  /** Tìm payment PENDING quá lâu. Dùng cho: cron job xử lý timeout payment. */
  List<Payment> findPendingOlderThan(LocalDateTime threshold);

  /**
   * Tổng doanh thu thực nhận của seller (sau phí sàn). Trả BigDecimal.ZERO nếu chưa có payment nào.
   */
  BigDecimal sumNetRevenueBySeller(Long sellerId);

  // ===== WRITE =====

  /** Lưu payment mới hoặc cập nhật */
  Payment save(Payment payment);

  /**
   * Cập nhật status của payment. Dùng khi cổng thanh toán callback. Trả boolean để biết có cập nhật
   * được không.
   */
  boolean updateStatus(Long paymentId, Payment.PaymentStatus newStatus, String transactionRef);
}
