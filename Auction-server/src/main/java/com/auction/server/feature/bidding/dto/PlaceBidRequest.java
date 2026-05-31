package com.auction.server.feature.bidding.dto;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu đặt giá từ tầng trên (Controller/Socket Handler).
 *
 * Tại sao cần DTO riêng thay vì nhận tham số trực tiếp?
 *   1. Gom nhóm: 3 tham số thành 1 object → dễ truyền qua nhiều tầng
 *   2. Validate tập trung: Service validate 1 chỗ thay vì từng tham số
 *   3. Tách biệt: Controller biết HTTP, Service biết nghiệp vụ
 *      → DTO là "hợp đồng" giữa hai tầng
 *   4. Thay đổi dễ: thêm field mới không ảnh hưởng signature method
 *
 * Tại sao Long cho ID (không phải int)?
 *   int max = 2,147,483,647 (~2 tỷ).
 *   Hệ thống đấu giá lớn có thể vượt 2 tỷ bid/auction.
 *   Long max = 9,223,372,036,854,775,807 → thực tế không bao giờ đầy.
 *   DB schema dùng BIGINT → Java dùng Long để khớp kiểu.
 *
 * Tại sao BigDecimal cho tiền (không phải double)?
 *   double là floating point → không chính xác:
 *     0.1 + 0.2 = 0.30000000000000004 (lỗi dấu phẩy động)
 *   Với tiền: 500000.10 + 200000.20 phải = 700000.30 chính xác tuyệt đối.
 *   BigDecimal lưu số thập phân chính xác → bắt buộc dùng cho tiền.
 */
public class PlaceBidRequest {

    private Long auctionSessionId;  // ID phiên đấu giá — Long khớp BIGINT
    private Long bidderId;          // ID người đặt giá — Long khớp BIGINT
    private BigDecimal bidAmount;   // Số tiền đặt giá — BigDecimal cho tiền

    public PlaceBidRequest() {
        // Constructor rỗng cần thiết nếu dùng JSON deserialization
    }

    public PlaceBidRequest(Long auctionSessionId,
                           Long bidderId,
                           BigDecimal bidAmount) {
        this.auctionSessionId = auctionSessionId;
        this.bidderId         = bidderId;
        this.bidAmount        = bidAmount;
    }

    public Long getAuctionSessionId() { return auctionSessionId; }
    public void setAuctionSessionId(Long auctionSessionId) {
        this.auctionSessionId = auctionSessionId;
    }

    public Long getBidderId() { return bidderId; }
    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
}