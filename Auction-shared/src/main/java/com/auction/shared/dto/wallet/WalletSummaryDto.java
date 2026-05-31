package com.auction.shared.dto.wallet;

import java.math.BigDecimal;

/** Số dư ví hiện tại của người dùng. */
public class WalletSummaryDto {
    private Long userId;
    private BigDecimal balance;

    public WalletSummaryDto() {}

    public WalletSummaryDto(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
