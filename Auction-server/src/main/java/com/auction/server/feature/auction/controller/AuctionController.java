package com.auction.server.feature.auction.controller;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.service.AuctionService;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.category.CategoryDto;
import com.auction.shared.protocol.JsonSupport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * AuctionController: nhận request từ RequestDispatcher, gọi AuctionService, trả Response.
 *
 * Trách nhiệm duy nhất:
 *   - Parse JSON body → DTO
 *   - Gọi Service
 *   - Bắt exception → trả Response.fail() có message
 *   - Không chứa logic nghiệp vụ
 *
 * Exception strategy (đơn giản hóa):
 *   - AuctionException → lỗi nghiệp vụ → trả message cho client
 *   - Exception         → lỗi hệ thống  → trả message chung
 *   Không dùng BusinessException hierarchy để giữ code đơn giản.
 */
public class AuctionController {

    private final AuctionService auctionService;
    private final Gson gson = JsonSupport.createGson(); // Gson thread-safe, dùng chung được

    // =========================================================================
    // KIẾN TRÚC DI (Constructor Injection):
    // Xóa bỏ hoàn toàn Constructor mặc định tự ý 'new' các tầng Repository/Service.
    // Việc này giúp Controller hoàn toàn độc lập với hạ tầng dữ liệu cụ thể (Hibernate).
    // =========================================================================
    // ─── Constructor test: inject mock service
    //-Contructor sử dụng chính
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // =====================================================
    // Trả danh sách phiên đấu giá
    // =====================================================

    /**
     * Lấy danh sách phiên đấu giá đang hoạt động.
     * Request body: { "page": 0, "size": 20 }
     */
    public Response<List<AuctionResponse>> getAllAuctions(String requestBody) {
        try {
            //trả ds trang phiên đấu giá bắt đầu từ 0, và có 20 phiên
            JsonObject obj = gson.fromJson(requestBody, JsonObject.class);
            int page = obj != null && obj.has("page") ? obj.get("page").getAsInt() : 0;
            int size = obj != null && obj.has("size") ? obj.get("size").getAsInt() : 20;

            return Response.success("Auctions loaded", auctionService.getAllAuctions(page, size));

        } catch (AuctionException e) {
            // Lỗi nghiệp vụ — message có nghĩa, trả thẳng cho client
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            // Lỗi hệ thống — không lộ chi tiết kỹ thuật ra client
            return Response.fail("System error while getting auctions");
        }
    }


    /**
     * Lấy danh mục sản phẩm từ bảng categories trong database.
     * Client dùng kết quả này để hiển thị ComboBox tên danh mục thay vì bắt nhập categoryId.
     */
    public Response<List<CategoryDto>> getCategories(String requestBody) {
        try {
            return Response.success("Categories loaded", auctionService.listCategories());
        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while getting categories");
        }
    }

    // =====================================================
    // GET AUCTION DETAIL
    // =====================================================

    /**
     * Lấy chi tiết 1 phiên đấu giá.
     * Request body: { "auctionId": 42 }
     */
    public Response<AuctionDetailResponse> getAuctionDetail(String requestBody) {
        try {
            JsonObject obj = gson.fromJson(requestBody, JsonObject.class);
            long auctionId = obj.get("auctionId").getAsLong();

            return Response.success("Auction detail loaded", auctionService.getAuctionDetail(auctionId));

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while getting auction detail");
        }
    }

    // =====================================================
    // CREATE AUCTION
    // =====================================================

    /**
     * Tạo phiên đấu giá mới.
     * Request body: JSON của CreateAuctionRequest
     */
    public Response<AuctionResponse> createAuction(String requestBody) {
        try {
            CreateAuctionRequest request = gson.fromJson(requestBody, CreateAuctionRequest.class);

            return Response.success("Auction created", auctionService.createAuction(request));

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("System error while creating auction");
        }
    }
}
