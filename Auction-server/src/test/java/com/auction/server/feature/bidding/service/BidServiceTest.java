package com.auction.server.feature.bidding.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.User;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.bidding.repository.BidRepository;
import com.auction.server.feature.bidding.repository.PaymentRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cho BidService.
 *
 * <p>Phạm vi test: - placeBid(): validate PlaceBidRequest trước khi vào transaction -
 * getBidHistory(): validate auctionId và giới hạn limit
 *
 * <p>Kiến trúc test: Validate của BidService nằm trong validatePlaceBidRequest() — gọi TRƯỚC
 * DbExecutor. → Có thể test độc lập mà không cần Hibernate Session.
 *
 * <p>Các trường hợp Business Logic trong transaction (auction không active, giá không hợp lệ...)
 * cần Integration Test với H2 hoặc mockStatic DbExecutor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BidService Tests")
class BidServiceTest {

  @Mock private AuctionSessionRepository auctionSessionRepository;
  @Mock private BidRepository bidRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private UserRepository userRepository;
  @Mock private WalletTransactionRepository walletTransactionRepository;
  private BidService bidService;

  @BeforeEach
  void setUp() {
    bidService =
        new BidService(
            auctionSessionRepository,
            bidRepository,
            paymentRepository,
            userRepository,
            walletTransactionRepository);
  }

  // =========================================================
  // HELPER — tạo PlaceBidRequest hợp lệ
  // =========================================================

  private PlaceBidRequest validPlaceBidRequest() {
    return new PlaceBidRequest(
        1L, // auctionSessionId
        2L, // bidderId
        new BigDecimal("600000") // bidAmount (> 0)
        );
  }

  private User buildUser(Long id) {
    User u = new User();
    u.setId(id);
    u.setUsername("user" + id);
    return u;
  }

  private AuctionSession buildActiveAuction(Long id, BigDecimal currentPrice) {
    AuctionSession a = new AuctionSession();
    // Dùng reflection để set private fields vì entity không có setter public cho tất cả
    try {
      setPrivateField(a, "auctionId", id);
      setPrivateField(a, "currentPrice", currentPrice);
      setPrivateField(a, "status", AuctionSession.AuctionStatus.ACTIVE);
      setPrivateField(a, "endTime", java.time.LocalDateTime.now().plusDays(1));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return a;
  }

  private void setPrivateField(Object obj, String name, Object value) throws Exception {
    var field = obj.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(obj, value);
  }

  // =========================================================
  // placeBid() — Validate request (TRƯỚC transaction)
  // =========================================================

  @Nested
  @DisplayName("placeBid() — Validate input")
  class PlaceBidValidation {

    @Test
    @DisplayName("Ném BidException khi request null")
    void placeBid_nullRequest_throwsBidException() {
      assertThatThrownBy(() -> bidService.placeBid(null))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("PlaceBidRequest không được null");
    }

    @Test
    @DisplayName("Ném BidException khi auctionSessionId null")
    void placeBid_nullAuctionSessionId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(null, 2L, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionSessionId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi auctionSessionId = 0")
    void placeBid_zeroAuctionSessionId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(0L, 2L, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionSessionId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi auctionSessionId âm")
    void placeBid_negativeAuctionSessionId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(-5L, 2L, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionSessionId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi bidderId null")
    void placeBid_nullBidderId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, null, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidderId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi bidderId = 0")
    void placeBid_zeroBidderId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, 0L, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidderId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi bidderId âm")
    void placeBid_negativeBidderId_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, -1L, new BigDecimal("100000"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidderId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi bidAmount null")
    void placeBid_nullBidAmount_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, 2L, null);

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidAmount phải > 0");
    }

    @Test
    @DisplayName("Ném BidException khi bidAmount = 0")
    void placeBid_zeroBidAmount_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, 2L, BigDecimal.ZERO);

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidAmount phải > 0");
    }

    @Test
    @DisplayName("Ném BidException khi bidAmount âm")
    void placeBid_negativeBidAmount_throwsBidException() {
      PlaceBidRequest req = new PlaceBidRequest(1L, 2L, new BigDecimal("-500"));

      assertThatThrownBy(() -> bidService.placeBid(req))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("BidAmount phải > 0");
    }
  }

  // =========================================================
  // getBidHistory() — Validate auctionId
  // =========================================================

  @Nested
  @DisplayName("getBidHistory() — Validate auctionId")
  class GetBidHistoryValidation {

    @Test
    @DisplayName("Ném BidException khi auctionId null")
    void getBidHistory_nullAuctionId_throwsBidException() {
      assertThatThrownBy(() -> bidService.getBidHistory(null, 10))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi auctionId = 0")
    void getBidHistory_zeroAuctionId_throwsBidException() {
      assertThatThrownBy(() -> bidService.getBidHistory(0L, 10))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionId không hợp lệ");
    }

    @Test
    @DisplayName("Ném BidException khi auctionId âm")
    void getBidHistory_negativeAuctionId_throwsBidException() {
      assertThatThrownBy(() -> bidService.getBidHistory(-10L, 10))
          .isInstanceOf(BidException.class)
          .hasMessageContaining("AuctionId không hợp lệ");
    }
  }

  // =========================================================
  // PlaceBidRequest — Unit test DTO
  // =========================================================

  @Nested
  @DisplayName("PlaceBidRequest — getters/setters")
  class PlaceBidRequestDtoTest {

    @Test
    @DisplayName("Constructor đầy đủ gán đúng giá trị")
    void constructor_setsAllFields() {
      Long auctionId = 10L;
      Long bidderId = 20L;
      BigDecimal amount = new BigDecimal("750000");

      PlaceBidRequest req = new PlaceBidRequest(auctionId, bidderId, amount);

      org.assertj.core.api.Assertions.assertThat(req.getAuctionSessionId()).isEqualTo(auctionId);
      org.assertj.core.api.Assertions.assertThat(req.getBidderId()).isEqualTo(bidderId);
      org.assertj.core.api.Assertions.assertThat(req.getBidAmount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("Setter ghi đè giá trị ban đầu")
    void setters_overrideInitialValues() {
      PlaceBidRequest req = new PlaceBidRequest();
      req.setAuctionSessionId(5L);
      req.setBidderId(7L);
      req.setBidAmount(new BigDecimal("300000"));

      org.assertj.core.api.Assertions.assertThat(req.getAuctionSessionId()).isEqualTo(5L);
      org.assertj.core.api.Assertions.assertThat(req.getBidderId()).isEqualTo(7L);
      org.assertj.core.api.Assertions.assertThat(req.getBidAmount()).isEqualByComparingTo("300000");
    }
  }
}
