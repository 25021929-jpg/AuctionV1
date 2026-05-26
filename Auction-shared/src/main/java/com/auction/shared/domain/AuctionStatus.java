package com.auction.shared.domain;

/**
 * Auction lifecycle states (assignment): OPEN → RUNNING → FINISHED → PAID/CANCELED.
 */
public enum AuctionStatus {
    OPEN,
    RUNNING,
    FINISHED,
    PAID,
    CANCELED;

    /**
     * Parse status từ string (tolerant).
     *
     * <p>Cho phép null/blank và một số biến thể phổ biến để client/server "khớp" dễ hơn.
     *
     * @param raw status có thể null hoặc khác format
     * @return AuctionStatus hoặc null nếu không parse được
     */
    public static AuctionStatus fromString(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase();
        if (s.isEmpty()) return null;
        try {
            return AuctionStatus.valueOf(s);
        } catch (IllegalArgumentException ex) {
            // Một số hệ thống dùng biến thể British spelling
            if ("CANCELLED".equals(s)) return CANCELED;
            return null;
        }
    }

    /**
     * @return true nếu trạng thái cho phép đặt giá.
     */
    public boolean isBiddable() {
        return this == OPEN || this == RUNNING;
    }
}
