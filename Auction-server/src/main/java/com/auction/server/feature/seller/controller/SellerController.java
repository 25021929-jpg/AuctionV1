package com.auction.server.feature.seller.controller;

import com.auction.server.feature.seller.SellerException;
import com.auction.server.feature.seller.service.SellerService;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.seller.CreateSellerItemRequest;
import com.auction.shared.dto.seller.DeleteSellerItemRequest;
import com.auction.shared.dto.seller.SellerItemDto;
import com.auction.shared.dto.seller.UpdateSellerItemRequest;
import com.auction.shared.protocol.JsonSupport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Socket-facing controller for seller dashboard actions.
 *
 * <p>The dispatcher passes only WireMessage.data as raw JSON. This controller parses that
 * JSON into shared DTOs, delegates business rules to SellerService, then returns Response
 * for ClientHandler to wrap back into a WireMessage response.</p>
 */
public class SellerController {

    private final SellerService sellerService;
    private final Gson gson = JsonSupport.createGson();

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    public Response<List<SellerItemDto>> listMyItems(String requestBody) {
        try {
            // There is no authenticated socket session yet, so the client sends sellerId
            // explicitly. When token/session support is added, this should come from server state.
            JsonObject obj = gson.fromJson(requestBody, JsonObject.class);
            long sellerId = getLong(obj, "sellerId", 0);
            int page = getInt(obj, "page", 0);
            int size = getInt(obj, "size", 50);
            return Response.success("Seller items loaded", sellerService.listMyItems(sellerId, page, size));
        } catch (SellerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while loading seller items");
        }
    }

    public Response<SellerItemDto> createItem(String requestBody) {
        try {
            // CreateSellerItemRequest is shared with the client, so field names stay aligned
            // across the wire: sellerId/categoryId/name/startPrice/startTime/endTime.
            CreateSellerItemRequest request = gson.fromJson(requestBody, CreateSellerItemRequest.class);
            return Response.success("Seller item created", sellerService.createItem(request));
        } catch (SellerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while creating seller item");
        }
    }

    public Response<SellerItemDto> updateItem(String requestBody) {
        try {
            UpdateSellerItemRequest request = gson.fromJson(requestBody, UpdateSellerItemRequest.class);
            return Response.success("Seller item updated", sellerService.updateItem(request));
        } catch (SellerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while updating seller item");
        }
    }

    public Response<Void> deleteItem(String requestBody) {
        try {
            // Delete maps to a cancel/archive operation in SellerService, not a hard DB delete.
            DeleteSellerItemRequest request = gson.fromJson(requestBody, DeleteSellerItemRequest.class);
            sellerService.deleteItem(request);
            return Response.success("Seller item deleted", null);
        } catch (SellerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while deleting seller item");
        }
    }

    private long getLong(JsonObject obj, String field, long defaultValue) {
        if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
            return defaultValue;
        }
        return obj.get(field).getAsLong();
    }

    private int getInt(JsonObject obj, String field, int defaultValue) {
        if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
            return defaultValue;
        }
        return obj.get(field).getAsInt();
    }
}
