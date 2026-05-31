package com.auction.shared.dto.wallet;

import java.math.BigDecimal;

/** Request nạp tiền vào ví tài khoản. Amount phải là số nguyên dương. */
public class DepositRequest {
  private Long userId;
  private BigDecimal amount;

  public DepositRequest() {}

  public DepositRequest(Long userId, BigDecimal amount) {
    this.userId = userId;
    this.amount = amount;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}
