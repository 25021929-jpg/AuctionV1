package com.auction.server.feature.bidding.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.database.HibernateUtil;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.Bid;
import com.auction.server.entity.User;
import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.BidResponse;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.bidding.repository.AuctionSessionRepository;
import com.auction.server.feature.bidding.repository.BidRepository;
import com.auction.server.feature.bidding.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.bidding.repository.HibernateBidRepository;
import com.auction.server.feature.bidding.repository.HibernatePaymentRepository;
import com.auction.server.feature.bidding.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BidService {

    private final AuctionSessionRepository auctionSessionRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;

    // Constructor với Dependency Injection
    public BidService(
            AuctionSessionRepository auctionSessionRepository,
            BidRepository bidRepository,
            PaymentRepository paymentRepository
    ) {
        this.auctionSessionRepository = auctionSessionRepository;
        this.bidRepository = bidRepository;
        this.paymentRepository = paymentRepository;
    }

    // Constructor mặc định với singleton initialization
    public BidService() {
        this(
            new HibernateAuctionSessionRepository(HibernateUtil.getSessionFactory()),
            new HibernateBidRepository(HibernateUtil.getSessionFactory()),
            new HibernatePaymentRepository(HibernateUtil.getSessionFactory())
        );
    }

    /**
     * Đặt giá cho phiên đấu giá.
     *
     * Logic:
     * 1. Validate input
     * 2. Lock AuctionSession (PESSIMISTIC_WRITE) để tránh race condition
     * 3. Check phiên có đang hoạt động không
     * 4. Check giá đặt có hợp lệ không (>= current_price + step)
     * 5. Tạo Bid mới
     * 6. Update AuctionSession (currentPrice, winner, totalBids)
     * 7. Commit → Lock release
     *
     * Transaction scopes: DbExecutor.runAndReturn() đảm bảo atomicity.
     */
    public BidResponse placeBid(PlaceBidRequest request) {
        validatePlaceBidRequest(request);

        try {
            return DbExecutor.runAndReturn(() -> {
                // Lấy AuctionSession với lock (PESSIMISTIC_WRITE)
                // SELECT ... FOR UPDATE → block các transaction khác
                var auctionOpt = auctionSessionRepository.findByIdWithLock(request.getAuctionSessionId());

                if (auctionOpt.isEmpty()) {
                    throw new BidException("Auction session not found");
                }

                AuctionSession auction = auctionOpt.get();

                // Validate phiên đang hoạt động
                if (!auction.isActive()) {
                    throw new BidException("Auction is not active");
                }

                // Validate giá đặt hợp lệ
                BigDecimal bidAmount = BigDecimal.valueOf(request.getBidAmount());
                if (!auction.canAcceptBid(bidAmount)) {
                    throw new BidException(
                        String.format(
                            "Bid amount must be >= %.0f",
                            auction.getCurrentPrice()
                                .add(auction.getMinBidStep())
                                .doubleValue()
                        )
                    );
                }

                // Tạo Bid entity
                User bidder = new User();
                bidder.setId((long) request.getBidderId());

                Bid newBid = Bid.builder()
                        .auctionSession(auction)
                        .bidder(bidder)
                        .bidAmount(bidAmount)
                        .bidTime(LocalDateTime.now())
                        .isWinning(true)  // Là bid cao nhất hiện tại
                        .build();

                // Lưu Bid
                Bid savedBid = bidRepository.save(newBid);

                // Cập nhật AuctionSession (dirty checking tự động update khi flush)
                auction.applyNewBid(bidAmount, bidder);
                auctionSessionRepository.save(auction);

                // Return response
                return new BidResponse(
                        savedBid.getBidId().intValue(),
                        auction.getAuctionId().intValue(),
                        request.getBidderId(),
                        bidAmount.doubleValue(),
                        "Bid placed successfully"
                );
            });

        } catch (BidException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new BidException("System error while placing bid", e);
        }
    }

    /**
     * Lấy lịch sử đặt giá theo phiên (top N bids gần nhất).
     */
    public List<BidResponse> getBidHistory(Long auctionId, int limit) {
        if (auctionId == null || auctionId <= 0) {
            throw new BidException("Invalid auction id");
        }

        try {
            return DbExecutor.query(() -> {
                List<Bid> topBids = bidRepository.findTopByAuction(auctionId, limit);

                return topBids.stream()
                        .map(bid -> new BidResponse(
                                bid.getBidId().intValue(),
                                bid.getAuctionSession().getAuctionId().intValue(),
                                bid.getBidder().getId().intValue(),
                                bid.getBidAmount().doubleValue(),
                                "Bid history entry"
                        ))
                        .collect(Collectors.toList());
            });

        } catch (BidException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new BidException("System error while getting bid history", e);
        }
    }

    /**
     * Validate PlaceBidRequest
     */
    private void validatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new BidException("Place bid request is required");
        }

        if (request.getAuctionSessionId() <= 0) {
            throw new BidException("Invalid auction session id");
        }

        if (request.getBidderId() <= 0) {
            throw new BidException("Invalid bidder id");
        }

        if (request.getBidAmount() <= 0) {
            throw new BidException("Bid amount must be greater than 0");
        }
    }
}
