package com.auction.shared.dto.seller;

/** Request Seller xóa sản phẩm/phiên đấu giá. auctionId có thể null nếu server chỉ cần itemId. */
public record DeleteSellerItemRequest(long itemId, Long auctionId) {
}
