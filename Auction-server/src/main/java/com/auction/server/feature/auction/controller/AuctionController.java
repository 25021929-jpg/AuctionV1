package com.auction.server.feature.auction.controller;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.service.AuctionService;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller nhận request từ SocketHandler, gọi Service, trả Response.
 *
 * Pattern y hệt code gốc của bạn:
 *   - Nhận String body (JSON từ socket)
 *   - Gson parse → DTO
 *   - Gọi service
 *   - Trả Response.success() hoặc Response.fail()
 *
 * Gson được config để xử lý LocalDateTime
 * (mặc định Gson không tự parse LocalDateTime).
 */
public class AuctionController {

    private final AuctionService auctionService;
    private final Gson gson;

//Tạo phiên đấu giá lúc mấy h, kết thúc khi nào
//đổi kiểu dữ liệu từ chuỗi JSON sang Object Java để service có thể kiểm tra nếu qua thì gọi repository lưu vào database
    public AuctionController() {
        this.auctionService = new AuctionService();
        this.gson = new GsonBuilder()
                // Config Gson để phân tích LocalDateTime dạng "yyyy-MM-dd HH:mm:ss"
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .create();
    }

    // ===================================================================
    // LẤY DANH SÁCH TẤT CẢ PHIÊN
    // ===================================================================

    public Response<List<AuctionResponse>> getAllAuctions() {
        try {
            List<AuctionResponse> result = auctionService.getAllAuctions();
            return Response.success("Get auctions success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // ===================================================================
    // LẤY CHI TIẾT PHIÊN
    // ===================================================================

    /** body JSON: {"auctionId": 5} */
    public Response<AuctionDetailResponse> getAuctionDetail(String body) {
        try {
            int auctionId = gson.fromJson(body, AuctionIdRequest.class).auctionId();
            AuctionDetailResponse result = auctionService.getAuctionDetail(auctionId);
            return Response.success("Get auction detail success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // ===================================================================
    // TẠO PHIÊN ĐẤU GIÁ MỚI
    // ===================================================================

    /**
     * body JSON:
     * {
     *   "itemId": 1,
     *   "sellerId": 10,
     *   "startingPrice": 5000000,
     *   "startTime": "2026-05-21 08:00:00",
     *   "endTime":   "2026-05-21 10:00:00"
     * }
     */
    public Response<AuctionResponse> createAuction(String body) {
        try {
            CreateAuctionRequest request = gson.fromJson(body, CreateAuctionRequest.class);
            AuctionResponse result = auctionService.createAuction(request);
            return Response.success("Create auction success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // ===================================================================
    // HUỶ PHIÊN (Admin)
    // ===================================================================

    /**
     * body JSON: {"auctionId": 5}
     *
     * [FIX] Trước đây gọi auctionService.finishAuction() — sai vì finishAuction()
     * yêu cầu status=RUNNING, nên không hủy được phiên OPEN.
     * Nay gọi auctionService.cancelAuction() — xử lý đúng cả OPEN lẫn RUNNING.
     */
    public Response<Void> cancelAuction(String body) {
        try {
            int auctionId = gson.fromJson(body, AuctionIdRequest.class).auctionId();
            auctionService.cancelAuction(auctionId);
            return Response.success("Cancel auction success", null);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // ===================================================================
    // HELPER
    // ===================================================================

    //Gson chuyển file JSon thành object và cái này định nghĩa object đó
    private record AuctionIdRequest(int auctionId) {}
}

