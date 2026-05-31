package com.auction.server.feature.auction.scheduler;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.Payment;
import com.auction.server.entity.User;
import com.auction.server.entity.WalletTransaction;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.bidding.repository.BidRepository;
import com.auction.server.feature.bidding.repository.PaymentRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;
import com.auction.server.network.BroadcastService;
import com.auction.shared.protocol.ActionConstants;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Đồng bộ trạng thái phiên đấu giá theo thời gian thực.
 *
 * <p>Khi phiên chuyển sang ENDED, scheduler cũng chốt tiền bằng ví nội bộ: buyer bị trừ đúng giá
 * thắng, seller được cộng đúng giá thắng và cả hai bên có lịch sử giao dịch. PaymentRepository được
 * dùng như guard idempotent để tránh chốt tiền lặp lại nếu scheduler chạy nhiều lần.
 */
public final class AuctionStatusScheduler {

  private final AuctionSessionRepository auctionSessionRepository;
  private final PaymentRepository paymentRepository;
  private final BidRepository bidRepository;
  private final UserRepository userRepository;
  private final WalletTransactionRepository walletTransactionRepository;
  private final ScheduledExecutorService executor;

  public AuctionStatusScheduler(
      AuctionSessionRepository auctionSessionRepository,
      PaymentRepository paymentRepository,
      BidRepository bidRepository,
      UserRepository userRepository,
      WalletTransactionRepository walletTransactionRepository) {
    this.auctionSessionRepository = auctionSessionRepository;
    this.paymentRepository = paymentRepository;
    this.bidRepository = bidRepository;
    this.userRepository = userRepository;
    this.walletTransactionRepository = walletTransactionRepository;
    this.executor =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "auction-status-scheduler");
              thread.setDaemon(true);
              return thread;
            });
  }

  public void start() {
    executor.scheduleAtFixedRate(this::safeRefreshAndBroadcast, 0, 1, TimeUnit.SECONDS);
  }

  public void stop() {
    executor.shutdownNow();
  }

  private void safeRefreshAndBroadcast() {
    try {
      for (StatusChange change : refreshStatuses()) {
        // Status changes affect auction lists, seller dashboards and live rooms,
        // so broadcast to all connected clients. Client-side handlers filter by user/auction.
        BroadcastService.broadcastToAll(toStatusChangedEvent(change));
      }
    } catch (Exception ex) {
      System.out.println("Auction status scheduler error: " + ex.getMessage());
      ex.printStackTrace(System.out);
    }
  }

  private List<StatusChange> refreshStatuses() {
    return DbExecutor.runAndReturn(
        () -> {
          LocalDateTime now = LocalDateTime.now();
          List<StatusChange> changes = new ArrayList<>();

          for (AuctionSession auction : auctionSessionRepository.findScheduledToStart()) {
            if (auction.getEndTime() != null && auction.getEndTime().isAfter(now)) {
              auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
              auctionSessionRepository.save(auction);
              changes.add(toStatusChange(auction, AuctionSession.AuctionStatus.ACTIVE.name()));
            } else if (auction.getEndTime() != null) {
              endAuctionAndSettle(auction);
              changes.add(toStatusChange(auction, AuctionSession.AuctionStatus.ENDED.name()));
            }
          }

          for (AuctionSession auction : auctionSessionRepository.findExpired()) {
            endAuctionAndSettle(auction);
            changes.add(toStatusChange(auction, AuctionSession.AuctionStatus.ENDED.name()));
          }

          /*
           * Recovery path: older code, manual SQL, or an interrupted scheduler may leave an
           * auction already marked ENDED while no payment/wallet settlement exists yet.
           */
          for (AuctionSession auction : auctionSessionRepository.findEndedAwaitingSettlement()) {
            endAuctionAndSettle(auction);
          }

          return changes;
        });
  }

  private void endAuctionAndSettle(AuctionSession auction) {
    if (auction == null || auction.getAuctionId() == null) {
      return;
    }
    auction.setStatus(AuctionSession.AuctionStatus.ENDED);
    auctionSessionRepository.save(auction);

    if (auction.getWinner() == null
        || auction.getItem() == null
        || auction.getItem().getSeller() == null) {
      return;
    }
    if (paymentRepository.findByAuction(auction.getAuctionId()).isPresent()) {
      return; // đã chốt tiền trước đó, không ghi trùng
    }

    BigDecimal amount =
        auction.getCurrentPrice() == null ? BigDecimal.ZERO : auction.getCurrentPrice();
    if (amount.signum() <= 0) {
      return;
    }

    Long buyerId = auction.getWinner().getId();
    Long sellerId = auction.getItem().getSeller().getId();
    if (buyerId == null || sellerId == null || buyerId.equals(sellerId)) {
      return;
    }

    // Lock theo thứ tự id tăng dần để giảm nguy cơ deadlock nếu sau này có nhiều scheduler/worker.
    User first =
        userRepository
            .findByIdWithLock(Math.min(buyerId, sellerId))
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy user khi chốt ví"));
    User second =
        userRepository
            .findByIdWithLock(Math.max(buyerId, sellerId))
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy user khi chốt ví"));
    User buyer = first.getId().equals(buyerId) ? first : second;
    User seller = first.getId().equals(sellerId) ? first : second;

    Payment payment = new Payment();
    payment.setAuctionSession(auction);
    payment.setBuyer(buyer);
    payment.setSeller(seller);
    payment.setAmount(amount);
    payment.setPlatformFee(BigDecimal.ZERO);
    payment.setMethod(Payment.PaymentMethod.WALLET);

    if (buyer.getBalance().compareTo(amount) < 0) {
      // Không để số dư âm. Ghi FAILED để biết phiên đã được xử lý và không trừ tiền sai.
      payment.setStatus(Payment.PaymentStatus.FAILED);
      payment.setTransactionRef("WALLET-FAILED-" + auction.getAuctionId());
      paymentRepository.save(payment);
      return;
    }

    BigDecimal buyerBalanceAfter = buyer.getBalance().subtract(amount);
    BigDecimal sellerBalanceAfter = seller.getBalance().add(amount);
    buyer.setBalance(buyerBalanceAfter);
    seller.setBalance(sellerBalanceAfter);
    userRepository.save(buyer);
    userRepository.save(seller);

    payment.setStatus(Payment.PaymentStatus.COMPLETED);
    payment.setTransactionRef("WALLET-AUCTION-" + auction.getAuctionId());
    payment.setPaidAt(LocalDateTime.now());
    paymentRepository.save(payment);

    saveWalletTransaction(
        buyer,
        auction,
        WalletTransaction.TransactionType.AUCTION_PAYMENT,
        amount.negate(),
        buyerBalanceAfter,
        "Thanh toán phiên đấu giá #" + auction.getAuctionId());
    saveWalletTransaction(
        seller,
        auction,
        WalletTransaction.TransactionType.AUCTION_RECEIVE,
        amount,
        sellerBalanceAfter,
        "Nhận tiền từ phiên đấu giá #" + auction.getAuctionId());
  }

  private StatusChange toStatusChange(AuctionSession auction, String status) {
    Long auctionId = auction == null ? null : auction.getAuctionId();
    String itemName = null;
    Long sellerId = null;
    Long winnerId = null;
    String winnerUsername = null;
    List<Long> participantIds = List.of();

    if (auction != null && auction.getItem() != null) {
      itemName = auction.getItem().getItemName();
      if (auction.getItem().getSeller() != null) {
        sellerId = auction.getItem().getSeller().getId();
      }
    }
    if (auction != null && auction.getWinner() != null) {
      winnerId = auction.getWinner().getId();
      winnerUsername = auction.getWinner().getUsername();
    }
    if (auctionId != null && AuctionSession.AuctionStatus.ENDED.name().equals(status)) {
      participantIds = bidRepository.findDistinctBidderIdsByAuction(auctionId);
    }

    return new StatusChange(
        auctionId, status, itemName, sellerId, winnerId, winnerUsername, participantIds);
  }

  private void saveWalletTransaction(
      User user,
      AuctionSession auction,
      WalletTransaction.TransactionType type,
      BigDecimal amount,
      BigDecimal balanceAfter,
      String description) {
    WalletTransaction transaction = new WalletTransaction();
    transaction.setUser(user);
    transaction.setAuctionSession(auction);
    transaction.setType(type);
    transaction.setAmount(amount);
    transaction.setBalanceAfter(balanceAfter);
    transaction.setDescription(description);
    walletTransactionRepository.save(transaction);
  }

  private WireMessage toStatusChangedEvent(StatusChange change) {
    JsonObject data = new JsonObject();
    data.addProperty("auctionId", change.auctionId());
    data.addProperty("auctionSessionId", change.auctionId());
    data.addProperty("status", change.status());
    if (change.itemName() != null) data.addProperty("itemName", change.itemName());
    if (change.sellerId() != null) data.addProperty("sellerId", change.sellerId());
    if (change.winnerId() != null) data.addProperty("winnerId", change.winnerId());
    if (change.winnerUsername() != null)
      data.addProperty("winnerUsername", change.winnerUsername());
    if (change.participantIds() != null) {
      com.google.gson.JsonArray participants = new com.google.gson.JsonArray();
      for (Long participantId : change.participantIds()) {
        if (participantId != null) {
          participants.add(participantId);
        }
      }
      data.add("participantIds", participants);
    }
    data.addProperty("timestamp", System.currentTimeMillis());

    WireMessage event = new WireMessage();
    event.setType(WireMessageType.EVENT);
    event.setAction(ActionConstants.EVENT_AUCTION_STATUS_CHANGED);
    event.setData(data);
    return event;
  }

  private record StatusChange(
      Long auctionId,
      String status,
      String itemName,
      Long sellerId,
      Long winnerId,
      String winnerUsername,
      List<Long> participantIds) {}
}
