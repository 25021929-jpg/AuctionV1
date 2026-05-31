package com.auction.shared.domain;

/**
 * Trạng thái vòng đời phiên đấu giá dùng chung giữa client, shared và server.
 *
 * <p>Chuẩn hiện tại của dự án chỉ dùng một hệ trạng thái duy nhất:
 * <ul>
 *     <li>SCHEDULED: phiên đã tạo nhưng chưa tới giờ bắt đầu.</li>
 *     <li>ACTIVE: phiên đang mở, bidder được phép đặt giá.</li>
 *     <li>ENDED: phiên đã kết thúc tự nhiên theo thời gian.</li>
 *     <li>CANCELLED: phiên bị hủy thủ công bởi seller/admin.</li>
 * </ul>
 *
 * <p>Không đặt PAID trong AuctionStatus vì trạng thái thanh toán thuộc bảng
 * payments / wallet_transactions, không phải trạng thái vòng đời chính của phiên.
 */
public enum AuctionStatus {
    /** Phiên đã tạo nhưng chưa tới thời gian bắt đầu. */
    SCHEDULED,

    /** Phiên đang mở, có thể đặt giá. */
    ACTIVE,

    /** Phiên đã hết thời gian, không còn đặt giá. */
    ENDED,

    /** Phiên đã hủy. */
    CANCELLED;

    /**
     * Parse status từ string một cách an toàn.
     *
     * <p>Hệ enum cũ đã được bỏ, nhưng method này vẫn map các chuỗi legacy
     * sang status chuẩn để tránh vỡ dữ liệu/test cũ khi còn log hoặc response cũ:
     * OPEN -> SCHEDULED, RUNNING -> ACTIVE, FINISHED/PAID -> ENDED,
     * CANCELED -> CANCELLED.
     *
     * @param raw status có thể null hoặc khác format
     * @return AuctionStatus hoặc null nếu không parse được
     */
    public static AuctionStatus fromString(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase();
        if (s.isEmpty()) return null;

        return switch (s) {
            case "OPEN" -> SCHEDULED;
            case "RUNNING" -> ACTIVE;
            case "FINISHED", "PAID" -> ENDED;
            case "CANCELED" -> CANCELLED;
            default -> {
                try {
                    yield AuctionStatus.valueOf(s);
                } catch (IllegalArgumentException ex) {
                    yield null;
                }
            }
        };
    }

    /**
     * @return true nếu trạng thái cho phép đặt giá.
     */
    public boolean isBiddable() {
        return this == ACTIVE;
    }

    /**
     * @return true nếu phiên không còn nhận bid và cần làm mới ví/lịch sử giao dịch.
     */
    public boolean isFinishedLike() {
        return this == ENDED || this == CANCELLED;
    }
}
