package com.auction.shared.dto.wallet;

/** Request lấy lịch sử giao dịch ví. */
public class WalletTransactionHistoryRequest {
    private Long userId;
    private int limit = 50;

    public WalletTransactionHistoryRequest() {}

    public WalletTransactionHistoryRequest(Long userId, int limit) {
        this.userId = userId;
        this.limit = limit;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
