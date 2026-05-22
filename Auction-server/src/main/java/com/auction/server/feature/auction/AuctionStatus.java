package com.auction.server.feature.auction;

/**
 * Vòng đời phiên đấu giá.
 * Lưu dưới dạng String trong DB (column status).
 * định nghĩa tất cả trạng thái có thể có của 1 phiên đấu giá trong feature/auction.
 */

public enum AuctionStatus {
    OPEN,       // Tạo xong, chưa đến giờ bắt đầu
    RUNNING,    // Đang diễn ra, nhận bid
    FINISHED,   // Hết giờ, có người thắng
    CANCELED    // Hết giờ nhưng không có bid nào
}
