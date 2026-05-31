package com.auction.shared.dto.seller;

/** Request for deleting or cancelling a seller-owned item/auction. */
public record DeleteSellerItemRequest(long itemId, Long auctionId, long sellerId) {
  public DeleteSellerItemRequest(long itemId, Long auctionId) {
    this(itemId, auctionId, 0);
  }
}
