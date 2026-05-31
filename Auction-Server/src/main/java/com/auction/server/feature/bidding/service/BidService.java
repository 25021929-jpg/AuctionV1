package com.auction.server.feature.bidding.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.Bid;
import com.auction.server.entity.User;
import com.auction.server.entity.WalletTransaction;
import com.auction.server.feature.auction.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.auth.repository.HibernateUserRepository;
import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.BidResponse;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.bidding.repository.BidRepository;
import com.auction.server.feature.bidding.repository.HibernateBidRepository;
import com.auction.server.feature.bidding.repository.HibernatePaymentRepository;
import com.auction.server.feature.bidding.repository.PaymentRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.wallet.repository.HibernateWalletTransactionRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ nghiệp vụ đặt giá.
 *
 * Nguyên tắc Service Layer:
 *   - Chỉ chứa Business Logic — không chứa SQL, Hibernate, HTTP
 *   - Nhận DTO từ tầng trên, trả DTO về tầng trên
 *   - Không biết Session tồn tại — DbExecutor lo transaction
 *   - Phối hợp nhiều Repository trong 1 usecase
 *   - Quyết định khi nào cần lock (findByIdWithLock)
 *
 * Dependency Injection:
 *   Service nhận Interface qua constructor.
 *   Không biết implementation nào đang dùng.
 *   Main.java tạo implementation và inject vào.
 *   → DIP (Dependency Inversion Principle)
 *   → Dễ test: inject MockRepository thay vì HibernateRepository
 */
public class BidService {

    // Interface — không phải implementation cụ thể
    // Service không biết đây là HibernateXxx hay MockXxx
    private final AuctionSessionRepository auctionSessionRepository;
    private final BidRepository            bidRepository;
    private final PaymentRepository        paymentRepository;
    private final UserRepository           userRepository;
    @SuppressWarnings("unused")
    private final WalletTransactionRepository walletTransactionRepository;

    /**
     * Constructor duy nhất — nhận interface, không nhận implementation.
     *
     * Tại sao không có constructor mặc định tạo new HibernateXxx()?
     *   Constructor mặc định buộc Service phải biết HibernateXxx tồn tại.
     *   Vi phạm DIP: tầng cao (Service) phụ thuộc tầng thấp (HibernateXxx).
     *   Khó test: không thể inject MockRepository.
     *   Main.java mới là nơi tạo implementation và inject.
     */
    public BidService(AuctionSessionRepository auctionSessionRepository,
                      BidRepository            bidRepository,
                      PaymentRepository        paymentRepository,
                      UserRepository           userRepository,
                      WalletTransactionRepository walletTransactionRepository) {
        this.auctionSessionRepository = auctionSessionRepository;
        this.bidRepository            = bidRepository;
        this.paymentRepository        = paymentRepository;
        this.userRepository           = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    /** Constructor mặc định dùng cho Production */
    public BidService() {
        this(
            new HibernateAuctionSessionRepository(com.auction.server.database.HibernateUtil.getSessionFactory()),
            new HibernateBidRepository(com.auction.server.database.HibernateUtil.getSessionFactory()),
            new HibernatePaymentRepository(com.auction.server.database.HibernateUtil.getSessionFactory()),
            new HibernateUserRepository(com.auction.server.database.HibernateUtil.getSessionFactory()),
            new HibernateWalletTransactionRepository(com.auction.server.database.HibernateUtil.getSessionFactory())
        );
    }

    // ================================================================
    // PUBLIC METHODS — usecase của Service
    // ================================================================

    /**
     * Đặt giá cho phiên đấu giá.
     */
    public BidResponse placeBid(PlaceBidRequest request) {

        // Bước 1: Validate input — TRƯỚC khi mở transaction
        validatePlaceBidRequest(request);

        return DbExecutor.runAndReturn(() -> {

            // Bước 2: Lock auction row
            // 2. BẮT BUỘC dùng findByIdWithLock (PESSIMISTIC_WRITE) để khóa dòng dữ liệu của phiên đấu giá này lại.
            // Tránh việc 2 người cùng bid 1 lúc đọc ra cùng 1 mức giá hiện tại.
            AuctionSession auction = auctionSessionRepository
                    .findByIdWithLock(request.getAuctionSessionId())
                    .orElseThrow(() -> new BidException(
                            "Không tìm thấy phiên đấu giá: " + request.getAuctionSessionId()
                    ));

            refreshLockedAuctionStatus(auction);

            // Bước 3a: Kiểm tra phiên có đang active không
            // isActive() kiểm tra status == ACTIVE && endTime > now
            // Phải kiểm tra SAU khi lock — vì phiên có thể vừa kết thúc
            if (!auction.isActive()) {
                throw new BidException("Phiên đấu giá không còn active");
            }

            BigDecimal bidAmount = request.getBidAmount();
            if (!auction.canAcceptBid(bidAmount)) {
                throw new BidException("Giá đặt không hợp lệ (phải >= giá hiện tại + bước giá)");
            }

            User bidder = userRepository.findByIdWithLock(request.getBidderId())
                    .orElseThrow(() -> new BidException("Không tìm thấy người đặt giá"));

            if (auction.getItem() != null
                    && auction.getItem().getSeller() != null
                    && auction.getItem().getSeller().getId() != null
                    && auction.getItem().getSeller().getId().equals(request.getBidderId())) {
                throw new BidException("Seller không được tự đấu giá sản phẩm của chính mình");
            }

            if (bidAmount.compareTo(bidder.getBalance()) > 0) {
                throw new BidException(
                        "Số dư khả dụng không đủ. Hiện có: " + bidder.getBalance().toPlainString()
                                + " (không tính " + bidder.getBalanceOnHold().toPlainString() + " đang giữ ở phiên khác)"
                );
            }

            // Bước 3c: Kiểm tra người đặt không phải winner hiện tại
            // Không cho phép người đang thắng tự đặt lại — vô nghĩa nghiệp vụ
            if (auction.getWinner() != null &&
                    auction.getWinner().getId().equals(request.getBidderId())) {
                throw new BidException("Bạn đang giữ giá cao nhất rồi");
            }

            // Bước 4: Clear winning bids cũ
            // Trước khi tạo bid mới, reset isWinning của bid cũ về false
            // Nếu bỏ bước này → nhiều bid có isWinning=true cùng lúc
            //   → dữ liệu sai, khó xác định bid nào đang thắng
            // Dùng HQL UPDATE trong cùng session → cùng transaction với lock
            bidRepository.clearWinningBids(request.getAuctionSessionId());

            // ── ESCROW: Hoàn tiền người đang giữ giá cao nhất (nếu có) ──────────────
            // Trước khi bid mới thắng, người cũ đang bị hold tiền cần được hoàn lại
            // để họ có thể dùng tiền đó đặt phiên khác.
            User previousWinner = auction.getWinner();
            if (previousWinner != null) {
                // Load với lock để tránh race condition cập nhật balance
                User prevWinnerLocked = userRepository.findByIdWithLock(previousWinner.getId())
                        .orElse(null);
                if (prevWinnerLocked != null) {
                    BigDecimal prevBidAmount = auction.getCurrentPrice(); // giá cũ = số tiền đang bị hold
                    BigDecimal prevHold = prevWinnerLocked.getBalanceOnHold();
                    BigDecimal releaseAmount = prevBidAmount.min(prevHold); // không hoàn quá số đang hold
                    prevWinnerLocked.setBalanceOnHold(prevHold.subtract(releaseAmount));
                    prevWinnerLocked.setBalance(prevWinnerLocked.getBalance().add(releaseAmount));
                    userRepository.save(prevWinnerLocked);
                }
            }

            // ── ESCROW: Giữ tiền của bidder mới ──────────────────────────────────────
            // Trừ từ balance sang balance_on_hold — tiền vẫn thuộc về user nhưng bị khóa
            bidder.setBalance(bidder.getBalance().subtract(bidAmount));
            bidder.setBalanceOnHold(bidder.getBalanceOnHold().add(bidAmount));
            userRepository.save(bidder);

            // Bước 5: Lấy User proxy — KHÔNG query DB
            // getReference() trả về proxy chỉ có id
            // Hibernate dùng proxy để set FK khi persist Bid
            // Không cần load toàn bộ User → tiết kiệm 1 query
            //
            // TẠI SAO không dùng new User() như trước?
            //   new User() tạo Transient object — Hibernate không quản lý
            //   Khi persist Bid có bidder là Transient User
            //   → Hibernate cố INSERT User mới → lỗi UNIQUE constraint (email đã có)
            //   getReference() tạo Persistent proxy → Hibernate chỉ set FK, không INSERT
            // Bước 5: Tạo và lưu Bid mới
            Bid newBid = new Bid();
            newBid.setAuctionSession(auction);  // auction đang bị lock
            newBid.setBidder(bidder);           // proxy — không query DB
            newBid.setBidAmount(bidAmount);     // BigDecimal — chính xác
            newBid.setBidTime(LocalDateTime.now());
            newBid.setIsWinning(true);          // bid này đang thắng

            // save() ghi vào Thread-bound session → cùng transaction với lock
            Bid savedBid = bidRepository.save(newBid);

            // Bước 6: Cập nhật auction
            // applyNewBid() cập nhật: currentPrice, winner, totalBids
            // Gọi trên entity đang bị lock → Dirty Checking phát hiện thay đổi
            // Hibernate tự sinh UPDATE khi flush (trước commit)
            auction.applyNewBid(bidAmount, bidder);
            auctionSessionRepository.save(auction);

            // Bước 7: Trả về DTO
            return toResponse(savedBid);
        });
    }

    /**
     * Lấy lịch sử đặt giá của một phiên.
     */
    public List<BidResponse> getBidHistory(Long auctionId, int limit) {
        if (auctionId == null || auctionId <= 0) {
            throw new BidException("AuctionId không hợp lệ");
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));

        return DbExecutor.query(() -> {
            List<Bid> topBids = bidRepository.findTopByAuction(auctionId, safeLimit);
            return topBids.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        });
    }

    // ================================================================
    // MAPPING & HELPER
    // ================================================================

    private BidResponse toResponse(Bid bid) {
        return new BidResponse(
                bid.getBidId(),
                bid.getAuctionSession().getAuctionId(),
                bid.getBidder().getId(),
                bid.getBidder().getUsername(),
                bid.getBidAmount(),
                bid.getBidTime(),
                bid.getIsWinning()
        );
    }


    /**
     * Cập nhật trạng thái ngay trong transaction đặt giá.
     * Đây là lớp phòng thủ cuối cùng để phiên đã tới giờ có thể nhận bid
     * ngay cả khi scheduler chưa chạy hoặc client vừa tạo phiên xong.
     */
    private void refreshLockedAuctionStatus(AuctionSession auction) {
        LocalDateTime now = LocalDateTime.now();
        if (auction.getStatus() == AuctionSession.AuctionStatus.SCHEDULED
                && auction.getStartTime() != null
                && !auction.getStartTime().isAfter(now)
                && auction.getEndTime() != null
                && auction.getEndTime().isAfter(now)) {
            auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
            auctionSessionRepository.save(auction);
            return;
        }

    }

    /**
     * Validate PlaceBidRequest — kiểm tra cơ bản trước khi mở transaction.
     *
     * Validate ở đây:
     *   - null check
     *   - range check (> 0)
     *   - format check (BigDecimal không âm)
     *
     * Validate KHÔNG ở đây (thuộc Business Logic trong transaction):
     *   - Auction có active không → cần đọc DB có lock
     *   - Giá có hợp lệ không → cần đọc currentPrice có lock
     *   - User có tồn tại không → cần đọc DB
     */
    private void validatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new BidException("PlaceBidRequest không được null");
        }
        if (request.getAuctionSessionId() == null ||
                request.getAuctionSessionId() <= 0) {
            throw new BidException("AuctionSessionId không hợp lệ");
        }
        if (request.getBidderId() == null ||
                request.getBidderId() <= 0) {
            throw new BidException("BidderId không hợp lệ");
        }
        if (request.getBidAmount() == null ||
                request.getBidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            // compareTo(ZERO) <= 0: null hoặc âm hoặc bằng 0 đều bị reject
            throw new BidException("BidAmount phải > 0");
        }
    }
}
