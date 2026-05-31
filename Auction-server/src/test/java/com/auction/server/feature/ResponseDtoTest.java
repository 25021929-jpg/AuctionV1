package com.auction.server.feature;

import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.bidding.dto.BidResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * test cho AuctionResponse, AuctionDetailResponse, BidResponse
 * Coverage test cho các Response DTO.
 * Verify: constructor đầy đủ, no-arg constructor, getter/setter đúng giá trị.
 */
@DisplayName("Response DTO Tests")
class ResponseDtoTest {

    // =========================================================
    // AuctionResponse
    // =========================================================

    @Nested
    @DisplayName("AuctionResponse")
    class AuctionResponseTest {

        @Test
        @DisplayName("No-arg constructor tạo object không null")
        void noArgConstructor() {
            assertThat(new AuctionResponse()).isNotNull();
        }

        @Test
        @DisplayName("Full constructor gán đúng tất cả field")
        void fullConstructor_setsAllFields() {
            LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
            LocalDateTime end   = LocalDateTime.of(2026, 6, 4, 10, 0);

            AuctionResponse dto = new AuctionResponse(
                    1L, 10L, "Đồng hồ Seiko",
                    new BigDecimal("500000"), new BigDecimal("520000"),new BigDecimal(1000),10,
                    start, end, "ACTIVE"
            );

            assertThat(dto.getAuctionId()).isEqualTo(1L);
            assertThat(dto.getItemId()).isEqualTo(10L);
            assertThat(dto.getItemName()).isEqualTo("Đồng hồ Seiko");
            assertThat(dto.getStartingPrice()).isEqualByComparingTo("500000");
            assertThat(dto.getCurrentPrice()).isEqualByComparingTo("520000");
            assertThat(dto.getStartTime()).isEqualTo(start);
            assertThat(dto.getEndTime()).isEqualTo(end);
            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Setters ghi đè giá trị ban đầu")
        void setters_overrideValues() {
            AuctionResponse dto = new AuctionResponse();
            dto.setAuctionId(5L);
            dto.setItemId(15L);
            dto.setItemName("Tranh sơn dầu");
            dto.setStartingPrice(new BigDecimal("1000000"));
            dto.setCurrentPrice(new BigDecimal("1500000"));
            dto.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
            dto.setEndTime(LocalDateTime.of(2026, 7, 7, 9, 0));
            dto.setStatus("SCHEDULED");

            assertThat(dto.getAuctionId()).isEqualTo(5L);
            assertThat(dto.getItemId()).isEqualTo(15L);
            assertThat(dto.getItemName()).isEqualTo("Tranh sơn dầu");
            assertThat(dto.getStartingPrice()).isEqualByComparingTo("1000000");
            assertThat(dto.getCurrentPrice()).isEqualByComparingTo("1500000");
            assertThat(dto.getStatus()).isEqualTo("SCHEDULED");
        }

        @Test
        @DisplayName("Field mặc định là null khi dùng no-arg constructor")
        void defaultFieldsAreNull() {
            AuctionResponse dto = new AuctionResponse();
            assertThat(dto.getAuctionId()).isNull();
            assertThat(dto.getItemName()).isNull();
            assertThat(dto.getStatus()).isNull();
        }
    }

    // =========================================================
    // AuctionDetailResponse
    // =========================================================

    @Nested
    @DisplayName("AuctionDetailResponse")
    class AuctionDetailResponseTest {

        @Test
        @DisplayName("No-arg constructor tạo object không null")
        void noArgConstructor() {
            assertThat(new AuctionDetailResponse()).isNotNull();
        }

        @Test
        @DisplayName("Full constructor gán đúng tất cả field")
        void fullConstructor_setsAllFields() {
            LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
            LocalDateTime end   = LocalDateTime.of(2026, 6, 4, 10, 0);

            AuctionDetailResponse dto = new AuctionDetailResponse(
                    2L, 20L, "Xe máy Honda", "Xe còn mới 95%",
                    "Phương tiện", "Tran Van B",
                    new BigDecimal("15000000"), new BigDecimal("16000000"),
                    start, end, "ACTIVE"
            );

            assertThat(dto.getAuctionId()).isEqualTo(2L);
            assertThat(dto.getItemId()).isEqualTo(20L);
            assertThat(dto.getItemName()).isEqualTo("Xe máy Honda");
            assertThat(dto.getDescription()).isEqualTo("Xe còn mới 95%");
            assertThat(dto.getCategoryName()).isEqualTo("Phương tiện");
            assertThat(dto.getSellerName()).isEqualTo("Tran Van B");
            assertThat(dto.getStartingPrice()).isEqualByComparingTo("15000000");
            assertThat(dto.getCurrentPrice()).isEqualByComparingTo("16000000");
            assertThat(dto.getStartTime()).isEqualTo(start);
            assertThat(dto.getEndTime()).isEqualTo(end);
            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Setters ghi đè đúng giá trị")
        void setters_overrideValues() {
            AuctionDetailResponse dto = new AuctionDetailResponse();
            dto.setAuctionId(3L);
            dto.setItemId(30L);
            dto.setItemName("Laptop Dell");
            dto.setDescription("Máy tính xách tay cấu hình cao");
            dto.setCategoryName("Điện tử");
            dto.setSellerName("Le Van C");
            dto.setStartingPrice(new BigDecimal("20000000"));
            dto.setCurrentPrice(new BigDecimal("21000000"));
            dto.setStatus("ENDED");

            assertThat(dto.getAuctionId()).isEqualTo(3L);
            assertThat(dto.getDescription()).isEqualTo("Máy tính xách tay cấu hình cao");
            assertThat(dto.getCategoryName()).isEqualTo("Điện tử");
            assertThat(dto.getSellerName()).isEqualTo("Le Van C");
            assertThat(dto.getStatus()).isEqualTo("ENDED");
        }
    }

    // =========================================================
    // BidResponse
    // =========================================================

    @Nested
    @DisplayName("BidResponse")
    class BidResponseTest {

        @Test
        @DisplayName("No-arg constructor tạo object không null")
        void noArgConstructor() {
            assertThat(new BidResponse()).isNotNull();
        }

        @Test
        @DisplayName("Full constructor gán đúng tất cả field")
        void fullConstructor_setsAllFields() {
            LocalDateTime bidTime = LocalDateTime.of(2026, 6, 1, 14, 30, 5);

            BidResponse dto = new BidResponse(
                    100L, 1L, 2L,
                    new BigDecimal("510000"),
                    bidTime, true
            );

            assertThat(dto.getBidId()).isEqualTo(100L);
            assertThat(dto.getAuctionSessionId()).isEqualTo(1L);
            assertThat(dto.getBidderId()).isEqualTo(2L);
            assertThat(dto.getBidAmount()).isEqualByComparingTo("510000");
            assertThat(dto.getBidTime()).isEqualTo(bidTime);
            assertThat(dto.getIsWinning()).isTrue();
        }

        @Test
        @DisplayName("Setters ghi đè đúng giá trị")
        void setters_overrideValues() {
            BidResponse dto = new BidResponse();
            dto.setBidId(200L);
            dto.setAuctionSessionId(5L);
            dto.setBidderId(10L);
            dto.setBidAmount(new BigDecimal("750000"));
            dto.setBidTime(LocalDateTime.of(2026, 6, 2, 9, 0));
            dto.setIsWinning(false);

            assertThat(dto.getBidId()).isEqualTo(200L);
            assertThat(dto.getAuctionSessionId()).isEqualTo(5L);
            assertThat(dto.getBidderId()).isEqualTo(10L);
            assertThat(dto.getBidAmount()).isEqualByComparingTo("750000");
            assertThat(dto.getIsWinning()).isFalse();
        }

        @Test
        @DisplayName("isWinning = false khi bid không thắng")
        void isWinning_false_forLosingBid() {
            BidResponse dto = new BidResponse(
                    101L, 1L, 3L,
                    new BigDecimal("505000"),
                    LocalDateTime.now(), false
            );
            assertThat(dto.getIsWinning()).isFalse();
        }

        @Test
        @DisplayName("Field mặc định là null khi dùng no-arg constructor")
        void defaultFieldsAreNull() {
            BidResponse dto = new BidResponse();
            assertThat(dto.getBidId()).isNull();
            assertThat(dto.getBidAmount()).isNull();
            assertThat(dto.getIsWinning()).isNull();
        }
    }
}
