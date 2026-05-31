package com.auction.client.feature.auction.service;

import com.auction.shared.protocol.ActionConstants;
import com.auction.client.core.error.ApiException;
import com.auction.client.core.error.ResponseUtils;
import com.auction.shared.dto.auction.AuctionDetailDto;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import com.auction.client.feature.auction.mapper.AuctionDtoMapper;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.auction.shared.dto.auction.AuctionIdRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AuctionService phía client.
 *
 * Nguyên tắc:
 * - KHÔNG "đoán" schema cứng. Client parse theo kiểu tolerant:
 *   + chấp nhận nhiều key phổ biến (auctionId/id, itemName/name, currentPrice/currentBid...)
 *   + nếu thiếu field => set default và UI vẫn chạy.
 * - Khi team server chốt schema, chỉ cần sửa mapping trong class này.
 */
public class AuctionServiceImpl implements AuctionService {

    @Override
    public List<AuctionSummaryDto> fetchAuctions() throws IOException {
        Response<JsonElement> res = SocketClient.getInstance()
                .send(ActionConstants.AUCTION_GET_LIST, null, JsonElement.class);

        // Nếu server báo lỗi -> throw để controller show message phù hợp.
        JsonElement data = ResponseUtils.unwrap(ActionConstants.AUCTION_GET_LIST, res);
        JsonArray arr;
        if (data.isJsonArray()) {
            arr = data.getAsJsonArray();
        } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
            // Optional wrapper: { items: [...] }
            JsonElement items = data.getAsJsonObject().get("items");
            arr = items != null && items.isJsonArray() ? items.getAsJsonArray() : new JsonArray();
        } else {
            return Collections.emptyList();
        }

        List<AuctionSummaryDto> out = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            out.add(AuctionDtoMapper.toSummary(el.getAsJsonObject()));
        }
        return out;
    }

    @Override
    public AuctionDetailDto fetchAuctionDetail(long auctionId) throws IOException {
        AuctionIdRequest request = new AuctionIdRequest(auctionId);

        Response<JsonElement> res = SocketClient.getInstance()
                .send(ActionConstants.AUCTION_GET_DETAIL, request, JsonElement.class);

        JsonElement data = ResponseUtils.unwrap(ActionConstants.AUCTION_GET_DETAIL, res);
        if (data == null || !data.isJsonObject()) {
            throw new ApiException(ActionConstants.AUCTION_GET_DETAIL, "Dữ liệu chi tiết phiên đấu giá không hợp lệ");
        }
        return AuctionDtoMapper.toDetail(data.getAsJsonObject(), auctionId);
    }
}
