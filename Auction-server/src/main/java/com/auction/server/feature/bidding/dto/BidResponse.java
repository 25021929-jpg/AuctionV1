package com.auction.server.feature.bidding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về dữ liệu bid sau khi đặt giá thành công.
 *
 * Tại sao Service trả DTO thay vì Entity (Bid)?
 *
 *   1. Tránh LazyInitializationException:
 *      Entity Bid có @ManyToOne(fetch=LAZY) đến AuctionSession, User.
 *      Session đóng sau khi Service commit.
 *      Controller cố serialize Entity → gọi getter lazy → session đã chết → crash.
 *      DTO chỉ chứa primitive/value → không có lazy relationship → an toàn.
 *
 *   2. Tránh vòng lặp JSON vô tận:
 *      Bid → AuctionSession → List<Bid> → Bid → AuctionSession → ...
 *      JSON serializer gặp vòng lặp → StackOverflowError.
 *      DTO không có circular reference → an toàn.
 *
 *   3. Tách biệt DB model và API model:
 *      Đổi tên cột trong DB → chỉ sửa Entity + DTO mapping.
 *      Controller/Client không bị ảnh hưởng.
 *
 *   4. Chỉ trả dữ liệu cần thiết:
 *      Entity có nhiều field nội bộ (version, isActive...).
 *      DTO chỉ trả field client thực sự cần.
 *
 * Tại sao không có field "message"?
 *   message ("Bid placed successfully") là metadata của HTTP response.
 *   Không phải domain data của Bid.
 *   Tầng Controller/Response wrapper lo phần này.
 *   Service chỉ trả dữ liệu thuần — không quan tâm HTTP.
 */
public class BidResponse {

    // Long cho mọi ID — khớp BIGINT trong DB, tránh overflow
    private Long bidId;
    private Long auctionSessionId;
    private Long bidderId;

    // BigDecimal cho tiền — chính xác tuyệt đối
    private BigDecimal bidAmount;

    // Thêm bidTime: client cần biết bid được ghi lúc nào
    // Dùng cho: hiển thị "Đặt giá lúc 14:30:05", sort theo thời gian
    private LocalDateTime bidTime;

    // isWinning: client biết ngay bid này có đang thắng không
    // Dùng cho: UI highlight bid thắng, thông báo "Bạn đang thắng!"
    private Boolean isWinning;

    public BidResponse() {}

    public BidResponse(Long bidId, Long auctionSessionId, Long bidderId,
                       BigDecimal bidAmount, LocalDateTime bidTime,
                       Boolean isWinning) {
        this.bidId            = bidId;
        this.auctionSessionId = auctionSessionId;
        this.bidderId         = bidderId;
        this.bidAmount        = bidAmount;
        this.bidTime          = bidTime;
        this.isWinning        = isWinning;
    }

    // Getters & Setters
    public Long getBidId()                       { return bidId; }
    public void setBidId(Long bidId)             { this.bidId = bidId; }

    public Long getAuctionSessionId()            { return auctionSessionId; }
    public void setAuctionSessionId(Long id)     { this.auctionSessionId = id; }

    public Long getBidderId()                    { return bidderId; }
    public void setBidderId(Long bidderId)       { this.bidderId = bidderId; }

    public BigDecimal getBidAmount()             { return bidAmount; }
    public void setBidAmount(BigDecimal amount)  { this.bidAmount = amount; }

    public LocalDateTime getBidTime()            { return bidTime; }
    public void setBidTime(LocalDateTime time)   { this.bidTime = time; }

    public Boolean getIsWinning()                { return isWinning; }
    public void setIsWinning(Boolean isWinning)  { this.isWinning = isWinning; }
}