package com.auction.shared.dto.auction;

/** Request chỉ cần auctionId, dùng cho detail/subscribe/unsubscribe. */
public record AuctionIdRequest(long auctionId) {}
