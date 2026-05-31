package com.auction.shared.dto.seller;

/** Request Seller cập nhật sản phẩm/phiên đấu giá. */
public class UpdateSellerItemRequest extends CreateSellerItemRequest {
  private long itemId;
  private long auctionId;

  public UpdateSellerItemRequest() {}

  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }

  public long getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(long auctionId) {
    this.auctionId = auctionId;
  }
}
