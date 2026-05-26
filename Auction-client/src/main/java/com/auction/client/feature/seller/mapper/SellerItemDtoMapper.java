package com.auction.client.feature.seller.mapper;

import com.auction.client.core.util.JsonRead;
import com.auction.shared.dto.seller.SellerItemDto;
import com.google.gson.JsonObject;

/** Mapper JSON -> SellerItemDto. */
public final class SellerItemDtoMapper {

    private SellerItemDtoMapper() {}

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    public static SellerItemDto toDto(JsonObject o) {
        SellerItemDto dto = new SellerItemDto();

        Long itemId = JsonRead.optLong(o, "itemId", "id");
        if (itemId != null) dto.setItemId(itemId);

        Long auctionId = JsonRead.optLong(o, "auctionId");
        if (auctionId != null) dto.setAuctionId(auctionId);

        Long categoryId = JsonRead.optLong(o, "categoryId");
        if (categoryId != null) dto.setCategoryId(categoryId);

        dto.setName(nn(JsonRead.optString(o, "name", "itemName", "title")));
        dto.setDescription(nn(JsonRead.optString(o, "description", "desc")));
        dto.setCategoryName(nn(JsonRead.optString(o, "categoryName", "category")));
        dto.setStartPrice(orZero(JsonRead.optBigDecimal(o, "startPrice", "startingPrice")));
        dto.setCurrentPrice(orZero(JsonRead.optBigDecimal(o, "currentPrice", "currentBid", "highestBid", "price")));
        dto.setStatus(nn(JsonRead.optString(o, "status", "state")));
        dto.setStartTime(JsonRead.optDateTime(o, "startTime", "startsAt", "startAt"));
        dto.setEndTime(JsonRead.optDateTime(o, "endTime", "endsAt", "endAt"));
        return dto;
    }

    private static java.math.BigDecimal orZero(java.math.BigDecimal v) {
        return v == null ? java.math.BigDecimal.ZERO : v;
    }
}
