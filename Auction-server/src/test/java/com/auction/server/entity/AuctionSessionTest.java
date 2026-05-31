package com.auction.server.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test cho business logic của AuctionSession entity.
 *
 * Tập trung vào 3 method có logic thật:
 *   - isActive()       : kiểm tra trạng thái phiên
 *   - canAcceptBid()   : kiểm tra giá đặt có hợp lệ không
 *   - applyNewBid()    : cập nhật trạng thái sau khi có bid mới
 *
 * Không cần mock, không cần DB — tất cả là pure Java logic.
 */
@DisplayName("AuctionSession — Business Logic Tests")
class AuctionSessionTest {

    private AuctionSession auction;

    @BeforeEach
    void setUp() {
        auction = new AuctionSession();
        auction.setCurrentPrice(new BigDecimal("500000"));
        auction.setMinBidStep(new BigDecimal("1000"));
        auction.setTotalBids(0);
    }

    // helper: tạo phiên đang active
    private AuctionSession activeAuction() {
        auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusHours(2));
        return auction;
    }

    // =========================================================
    // isActive()
    // =========================================================

    @Nested
    @DisplayName("isActive()")
    class IsActiveTest {

        @Test
        @DisplayName("true khi status = ACTIVE và endTime chưa qua")
        void isActive_activeStatusFutureEnd_returnsTrue() {
            activeAuction();
            assertThat(auction.isActive()).isTrue();
        }

        @Test
        @DisplayName("false khi status = ACTIVE nhưng endTime đã qua")
        void isActive_activeStatusPastEnd_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
            auction.setEndTime(LocalDateTime.now().minusMinutes(1));
            assertThat(auction.isActive()).isFalse();
        }

        @Test
        @DisplayName("false khi status = SCHEDULED dù endTime chưa qua")
        void isActive_scheduledStatus_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.SCHEDULED);
            auction.setEndTime(LocalDateTime.now().plusDays(1));
            assertThat(auction.isActive()).isFalse();
        }

        @Test
        @DisplayName("false khi status = ENDED")
        void isActive_endedStatus_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.ENDED);
            auction.setEndTime(LocalDateTime.now().plusDays(1));
            assertThat(auction.isActive()).isFalse();
        }

        @Test
        @DisplayName("false khi status = CANCELED")
        void isActive_cancelledStatus_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.CANCELLED);
            auction.setEndTime(LocalDateTime.now().plusDays(1));
            assertThat(auction.isActive()).isFalse();
        }

        @Test
        @DisplayName("false khi endTime đúng bằng thời điểm hiện tại (không còn trước)")
        void isActive_endTimeIsNow_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
            // Đặt endTime trong quá khứ gần để chắc chắn isBefore() = false
            auction.setEndTime(LocalDateTime.now().minusNanos(1));
            assertThat(auction.isActive()).isFalse();
        }
    }

    // =========================================================
    // canAcceptBid()
    // =========================================================

    @Nested
    @DisplayName("canAcceptBid()")
    class CanAcceptBidTest {

        @Test
        @DisplayName("true khi amount = currentPrice + minBidStep (biên hợp lệ)")
        void canAcceptBid_exactMinimum_returnsTrue() {
            activeAuction();
            // 500000 + 1000 = 501000
            assertThat(auction.canAcceptBid(new BigDecimal("501000"))).isTrue();
        }

        @Test
        @DisplayName("true khi amount > currentPrice + minBidStep")
        void canAcceptBid_aboveMinimum_returnsTrue() {
            activeAuction();
            assertThat(auction.canAcceptBid(new BigDecimal("600000"))).isTrue();
        }

        @Test
        @DisplayName("false khi amount = currentPrice (bằng giá hiện tại, chưa đủ bước)")
        void canAcceptBid_equalCurrentPrice_returnsFalse() {
            activeAuction();
            assertThat(auction.canAcceptBid(new BigDecimal("500000"))).isFalse();
        }

        @Test
        @DisplayName("false khi amount = currentPrice + minBidStep - 1 (thiếu 1 đồng)")
        void canAcceptBid_oneUnderMinimum_returnsFalse() {
            activeAuction();
            // 501000 - 1 = 500999
            assertThat(auction.canAcceptBid(new BigDecimal("500999"))).isFalse();
        }

        @Test
        @DisplayName("false khi phiên không active dù giá hợp lệ")
        void canAcceptBid_notActive_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.ENDED);
            auction.setEndTime(LocalDateTime.now().plusDays(1));
            assertThat(auction.canAcceptBid(new BigDecimal("510000"))).isFalse();
        }

        @Test
        @DisplayName("false khi phiên đã hết giờ dù giá hợp lệ")
        void canAcceptBid_expiredAuction_returnsFalse() {
            auction.setStatus(AuctionSession.AuctionStatus.ACTIVE);
            auction.setEndTime(LocalDateTime.now().minusSeconds(1));
            assertThat(auction.canAcceptBid(new BigDecimal("510000"))).isFalse();
        }

        @Test
        @DisplayName("canAcceptBid với minBidStep khác mặc định")
        void canAcceptBid_customBidStep_respectsNewStep() {
            activeAuction();
            auction.setMinBidStep(new BigDecimal("50000"));
            // 500000 + 50000 = 550000
            assertThat(auction.canAcceptBid(new BigDecimal("550000"))).isTrue();
            assertThat(auction.canAcceptBid(new BigDecimal("549999"))).isFalse();
        }
    }

    // =========================================================
    // applyNewBid()
    // =========================================================

    @Nested
    @DisplayName("applyNewBid()")
    class ApplyNewBidTest {

        @Test
        @DisplayName("cập nhật currentPrice sau khi bid")
        void applyNewBid_updatesCurrentPrice() {
            BigDecimal newAmount = new BigDecimal("520000");
            User bidder = buildUser(1L);

            auction.applyNewBid(newAmount, bidder);

            assertThat(auction.getCurrentPrice()).isEqualByComparingTo("520000");
        }

        @Test
        @DisplayName("cập nhật winner sau khi bid")
        void applyNewBid_updatesWinner() {
            User bidder = buildUser(42L);
            auction.applyNewBid(new BigDecimal("520000"), bidder);

            assertThat(auction.getWinner()).isEqualTo(bidder);
            assertThat(auction.getWinner().getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("tăng totalBids thêm 1 sau mỗi bid")
        void applyNewBid_incrementsTotalBids() {
            User bidder = buildUser(1L);
            assertThat(auction.getTotalBids()).isEqualTo(0);

            auction.applyNewBid(new BigDecimal("510000"), bidder);
            assertThat(auction.getTotalBids()).isEqualTo(1);

            auction.applyNewBid(new BigDecimal("520000"), bidder);
            assertThat(auction.getTotalBids()).isEqualTo(2);
        }

        @Test
        @DisplayName("winner bị ghi đè khi có bid mới hơn")
        void applyNewBid_replacesWinnerOnSubsequentBid() {
            User firstBidder  = buildUser(1L);
            User secondBidder = buildUser(2L);

            auction.applyNewBid(new BigDecimal("510000"), firstBidder);
            auction.applyNewBid(new BigDecimal("520000"), secondBidder);

            assertThat(auction.getWinner()).isEqualTo(secondBidder);
            assertThat(auction.getCurrentPrice()).isEqualByComparingTo("520000");
            assertThat(auction.getTotalBids()).isEqualTo(2);
        }

        private User buildUser(Long id) {
            User u = new User();
            u.setId(id);
            u.setUsername("user" + id);
            return u;
        }
    }

    // =========================================================
    // equals() & hashCode()
    // =========================================================

    @Nested
    @DisplayName("equals() và hashCode()")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("Hai AuctionSession cùng auctionId thì bằng nhau")
        void equals_sameId_areEqual() {
            AuctionSession a1 = new AuctionSession();
            AuctionSession a2 = new AuctionSession();
            a1.setAuctionId(1L);
            a2.setAuctionId(1L);

            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("Hai AuctionSession khác auctionId thì không bằng nhau")
        void equals_differentId_areNotEqual() {
            AuctionSession a1 = new AuctionSession();
            AuctionSession a2 = new AuctionSession();
            a1.setAuctionId(1L);
            a2.setAuctionId(2L);

            assertThat(a1).isNotEqualTo(a2);
        }

        @Test
        @DisplayName("AuctionSession bằng chính nó (reflexive)")
        void equals_sameInstance_isEqual() {
            auction.setAuctionId(5L);
            assertThat(auction).isEqualTo(auction);
        }
    }
}