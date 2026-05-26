package com.auction.client.feature.auction.mapper;

import com.auction.client.core.util.JsonRead;
import com.auction.shared.dto.auction.AuctionDetailDto;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import com.auction.shared.domain.AuctionStatus;
import com.google.gson.JsonObject;

import java.time.LocalDateTime;

/**
 * Mapper chuyển JSON (từ server) -> DTO cho client.
 *
 * <p>Thiết kế tách riêng để:
 * - Dễ unit-test mapping mà không cần chạy server.
 * - Khi server chốt schema, chỉ sửa mapper.
 */
public final class AuctionDtoMapper {

    private AuctionDtoMapper() {}

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    public static AuctionSummaryDto toSummary(JsonObject o) {
        AuctionSummaryDto dto = new AuctionSummaryDto();

        dto.setAuctionId(orZero(JsonRead.optLong(o, "auctionId", "id")));
        dto.setItemName(nn(JsonRead.optString(o, "itemName", "name", "title")));
        dto.setCurrentPrice(orZero(JsonRead.optBigDecimal(o, "currentPrice", "currentBid", "highestBid", "price")));
        dto.setStatus(AuctionStatus.fromString(JsonRead.optString(o, "status", "state")));

        LocalDateTime end = JsonRead.optDateTime(o, "endTime", "endsAt", "endAt");
        dto.setEndTime(end);
        return dto;
    }

    public static AuctionDetailDto toDetail(JsonObject o, long fallbackAuctionId) {
        AuctionDetailDto dto = new AuctionDetailDto();

        Long idV = JsonRead.optLong(o, "auctionId", "id");
        long id = idV == null ? 0L : idV;
        dto.setAuctionId(id > 0 ? id : fallbackAuctionId);
        dto.setItemName(nn(JsonRead.optString(o, "itemName", "name", "title")));
        dto.setDescription(nn(JsonRead.optString(o, "description", "desc")));
        dto.setCategoryName(nn(JsonRead.optString(o, "categoryName", "category")));
        dto.setSellerName(nn(JsonRead.optString(o, "sellerName", "seller", "ownerName")));
        dto.setStartPrice(orZero(JsonRead.optBigDecimal(o, "startPrice", "startingPrice", "reservePrice")));
        dto.setCurrentPrice(orZero(JsonRead.optBigDecimal(o, "currentPrice", "currentBid", "highestBid", "price")));
        dto.setStatus(AuctionStatus.fromString(JsonRead.optString(o, "status", "state")));
        dto.setLeaderUsername(nn(JsonRead.optString(o, "leaderUsername", "leader", "highestBidder")));
        dto.setStartTime(JsonRead.optDateTime(o, "startTime", "startsAt", "startAt"));
        dto.setEndTime(JsonRead.optDateTime(o, "endTime", "endsAt", "endAt"));
        return dto;
    }

    private static long orZero(Long v) {
        return v == null ? 0L : v;
    }

    private static java.math.BigDecimal orZero(java.math.BigDecimal v) {
        return v == null ? java.math.BigDecimal.ZERO : v;
    }


}
