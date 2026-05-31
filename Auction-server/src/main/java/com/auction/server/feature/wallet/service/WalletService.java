package com.auction.server.feature.wallet.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.User;
import com.auction.server.entity.WalletTransaction;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;
import com.auction.shared.dto.wallet.WalletSummaryDto;
import com.auction.shared.dto.wallet.WalletTransactionDto;
import java.math.BigDecimal;
import java.util.List;

/** Service quản lý số dư ví và lịch sử giao dịch. */
public class WalletService {
  private final UserRepository userRepository;
  private final WalletTransactionRepository walletTransactionRepository;

  public WalletService(
      UserRepository userRepository, WalletTransactionRepository walletTransactionRepository) {
    this.userRepository = userRepository;
    this.walletTransactionRepository = walletTransactionRepository;
  }

  public WalletSummaryDto getSummary(Long userId) {
    validateUserId(userId);
    return DbExecutor.query(
        () -> {
          User user =
              userRepository
                  .findById(userId)
                  .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
          return new WalletSummaryDto(user.getId(), safeBalance(user.getBalance()));
        });
  }

  public WalletSummaryDto deposit(Long userId, BigDecimal amount) {
    validateUserId(userId);
    validateDepositAmount(amount);
    return DbExecutor.runAndReturn(
        () -> {
          User user =
              userRepository
                  .findByIdWithLock(userId)
                  .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
          BigDecimal newBalance = safeBalance(user.getBalance()).add(amount);
          user.setBalance(newBalance);
          userRepository.save(user);

          WalletTransaction tx = new WalletTransaction();
          tx.setUser(user);
          tx.setType(WalletTransaction.TransactionType.DEPOSIT);
          tx.setAmount(amount);
          tx.setBalanceAfter(newBalance);
          tx.setDescription("Nạp tiền vào tài khoản");
          walletTransactionRepository.save(tx);

          return new WalletSummaryDto(user.getId(), newBalance);
        });
  }

  public List<WalletTransactionDto> getTransactions(Long userId, int limit) {
    validateUserId(userId);
    int safeLimit = Math.max(1, Math.min(limit, 100));
    return DbExecutor.query(
        () ->
            walletTransactionRepository.findByUser(userId, safeLimit).stream()
                .map(this::toDto)
                .toList());
  }

  private void validateUserId(Long userId) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("UserId không hợp lệ");
    }
  }

  private void validateDepositAmount(BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("Số tiền nạp không được để trống");
    }
    if (amount.signum() <= 0) {
      throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
    }
    if (amount.stripTrailingZeros().scale() > 0) {
      throw new IllegalArgumentException("Số tiền nạp phải là số nguyên, không nhập tiền lẻ");
    }
  }

  private BigDecimal safeBalance(BigDecimal balance) {
    return balance == null ? BigDecimal.ZERO : balance;
  }

  private WalletTransactionDto toDto(WalletTransaction tx) {
    WalletTransactionDto dto = new WalletTransactionDto();
    dto.setTransactionId(tx.getTransactionId());
    dto.setUserId(tx.getUser() == null ? null : tx.getUser().getId());
    dto.setAuctionId(tx.getAuctionSession() == null ? null : tx.getAuctionSession().getAuctionId());
    dto.setType(tx.getType() == null ? null : tx.getType().name());
    dto.setAmount(tx.getAmount());
    dto.setBalanceAfter(tx.getBalanceAfter());
    dto.setDescription(tx.getDescription());
    dto.setCreatedAt(tx.getCreatedAt());
    return dto;
  }
}
