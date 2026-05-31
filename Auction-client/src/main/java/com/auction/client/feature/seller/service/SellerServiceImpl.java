package com.auction.client.feature.seller.service;

import com.auction.shared.protocol.ActionConstants;
import com.auction.client.core.error.ApiException;
import com.auction.client.core.error.ResponseUtils;
import com.auction.client.core.session.UserSession;
import com.auction.shared.dto.seller.SellerItemDto;
import com.auction.shared.dto.category.CategoryDto;
import com.auction.client.feature.seller.mapper.SellerItemDtoMapper;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.seller.CreateSellerItemRequest;
import com.auction.shared.dto.seller.DeleteSellerItemRequest;
import com.auction.shared.dto.seller.UpdateSellerItemRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SellerService phía client.
 *
 * <p>Nguyên tắc:
 * - Parse tolerant, không phụ thuộc schema cứng.
 * - Khi server chốt schema, chỉ cần sửa mapping tại đây.
 */
public class SellerServiceImpl implements SellerService {



    @Override
    public List<CategoryDto> listCategories() throws IOException {
        JsonObject request = new JsonObject();
        Response<JsonElement> res = SocketClient.getInstance()
                .send(ActionConstants.CATEGORY_GET_LIST, request, JsonElement.class);

        JsonElement data = ResponseUtils.unwrap(ActionConstants.CATEGORY_GET_LIST, res);
        JsonArray arr;
        if (data != null && data.isJsonArray()) {
            arr = data.getAsJsonArray();
        } else if (data != null && data.isJsonObject() && data.getAsJsonObject().has("categories")) {
            JsonElement categories = data.getAsJsonObject().get("categories");
            arr = categories != null && categories.isJsonArray() ? categories.getAsJsonArray() : new JsonArray();
        } else {
            return Collections.emptyList();
        }

        List<CategoryDto> out = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            CategoryDto dto = new CategoryDto();
            dto.setCategoryId(readLong(obj, "categoryId"));
            dto.setCategoryName(readString(obj, "categoryName"));
            dto.setSlug(readString(obj, "slug"));
            if (obj.has("parentId") && !obj.get("parentId").isJsonNull()) {
                dto.setParentId(obj.get("parentId").getAsLong());
            }
            if (dto.getCategoryId() > 0 && dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
                out.add(dto);
            }
        }
        return out;
    }

    @Override
    public List<SellerItemDto> listMyItems() throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("sellerId", requireCurrentUserId(ActionConstants.SELLER_ITEM_LIST_MY));

        Response<JsonElement> res = SocketClient.getInstance()
                .send(ActionConstants.SELLER_ITEM_LIST_MY, request, JsonElement.class);

        JsonElement data = ResponseUtils.unwrap(ActionConstants.SELLER_ITEM_LIST_MY, res);
        JsonArray arr;
        if (data.isJsonArray()) {
            arr = data.getAsJsonArray();
        } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
            JsonElement items = data.getAsJsonObject().get("items");
            arr = items != null && items.isJsonArray() ? items.getAsJsonArray() : new JsonArray();
        } else {
            return Collections.emptyList();
        }

        List<SellerItemDto> out = new ArrayList<>();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            out.add(SellerItemDtoMapper.toDto(el.getAsJsonObject()));
        }
        return out;
    }

    @Override
    public void createItem(SellerItemDto item) throws IOException {
        CreateSellerItemRequest request = toCreateRequest(item);
        Response<Void> res = SocketClient.getInstance()
                .send(ActionConstants.SELLER_ITEM_CREATE, request, Void.class);
        ResponseUtils.unwrap(ActionConstants.SELLER_ITEM_CREATE, res);
    }

    @Override
    public void updateItem(SellerItemDto item) throws IOException {
        UpdateSellerItemRequest request = new UpdateSellerItemRequest();
        copyEditableFields(item, request, ActionConstants.SELLER_ITEM_UPDATE);
        request.setItemId(item.getItemId());
        request.setAuctionId(item.getAuctionId());

        Response<Void> res = SocketClient.getInstance()
                .send(ActionConstants.SELLER_ITEM_UPDATE, request, Void.class);
        ResponseUtils.unwrap(ActionConstants.SELLER_ITEM_UPDATE, res);
    }

    @Override
    public void deleteItem(SellerItemDto item) throws IOException {
        if (item == null || item.getItemId() <= 0) {
            throw new IllegalArgumentException("Không xác định được sản phẩm cần xóa");
        }

        Long auctionId = item.getAuctionId() > 0 ? item.getAuctionId() : null;
        long sellerId = requireCurrentUserId(ActionConstants.SELLER_ITEM_DELETE);
        DeleteSellerItemRequest request = new DeleteSellerItemRequest(item.getItemId(), auctionId, sellerId);

        Response<Void> res = SocketClient.getInstance()
                .send(ActionConstants.SELLER_ITEM_DELETE, request, Void.class);
        ResponseUtils.unwrap(ActionConstants.SELLER_ITEM_DELETE, res);
    }

    private CreateSellerItemRequest toCreateRequest(SellerItemDto item) throws ApiException {
        CreateSellerItemRequest request = new CreateSellerItemRequest();
        copyEditableFields(item, request, ActionConstants.SELLER_ITEM_CREATE);
        return request;
    }

    private void copyEditableFields(SellerItemDto item, CreateSellerItemRequest request, String action) throws ApiException {
        request.setSellerId(requireCurrentUserId(action));
        request.setCategoryId(item.getCategoryId());
        request.setName(item.getName());
        request.setDescription(item.getDescription());
        request.setStartPrice(item.getStartPrice());
        request.setStartTime(item.getStartTime());
        request.setEndTime(item.getEndTime());
    }


    private String readString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private long readLong(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsLong() : 0L;
    }

    private long requireCurrentUserId(String action) throws ApiException {
        Long userId = UserSession.getInstance().getUserId();
        if (userId == null || userId <= 0) {
            throw new ApiException(action, "Bạn cần đăng nhập trước khi quản lý sản phẩm bán.");
        }
        return userId;
    }
}
