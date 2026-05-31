package com.auction.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Lịch sử biến động số dư ví của người dùng. Amount dùng dấu dương/âm theo nghiệp vụ: DEPOSIT,
 * AUCTION_RECEIVE: dương AUCTION_PAYMENT: âm
 */
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transaction_id")
  private Long transactionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id")
  private AuctionSession auctionSession;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private TransactionType type;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
  private BigDecimal balanceAfter;

  @Column(name = "description", length = 500)
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public WalletTransaction() {}

  public Long getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public AuctionSession getAuctionSession() {
    return auctionSession;
  }

  public void setAuctionSession(AuctionSession auctionSession) {
    this.auctionSession = auctionSession;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getBalanceAfter() {
    return balanceAfter;
  }

  public void setBalanceAfter(BigDecimal balanceAfter) {
    this.balanceAfter = balanceAfter;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WalletTransaction that)) return false;
    return Objects.equals(transactionId, that.transactionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId);
  }

  public enum TransactionType {
    DEPOSIT,
    AUCTION_PAYMENT,
    AUCTION_RECEIVE
  }
}
