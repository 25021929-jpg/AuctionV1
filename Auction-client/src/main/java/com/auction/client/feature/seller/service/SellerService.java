package com.auction.client.feature.seller.service;

import com.auction.shared.dto.category.CategoryDto;
import com.auction.shared.dto.seller.SellerItemDto;
import java.io.IOException;
import java.util.List;

public interface SellerService {

  List<SellerItemDto> listMyItems() throws IOException;

  /** Lấy danh mục sản phẩm từ server/database để render ComboBox, không hard-code ở client. */
  List<CategoryDto> listCategories() throws IOException;

  void createItem(SellerItemDto item) throws IOException;

  void updateItem(SellerItemDto item) throws IOException;

  /** Xóa theo DTO đầy đủ để tránh gửi auctionId giả. */
  void deleteItem(SellerItemDto item) throws IOException;

  /** Giữ overload này cho test/code cũ. Nếu không có auctionId thì request gửi null. */
  default void deleteItem(long itemId) throws IOException {
    SellerItemDto item = new SellerItemDto();
    item.setItemId(itemId);
    deleteItem(item);
  }
}
