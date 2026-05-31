package com.auction.server.feature.auction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cho AuctionService.
 *
 * <p>Phạm vi test: - getAllAuctions(): validate tham số page/size - getAuctionDetail(): validate
 * auctionId - createAuction(): validate toàn bộ các trường của CreateAuctionRequest
 *
 * <p>Lưu ý thiết kế: AuctionService gọi DbExecutor bên trong các method. Validate format được thực
 * hiện TRƯỚC khi gọi DbExecutor, nên các test validate KHÔNG cần mock transaction.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService Tests")
class AuctionServiceTest {

  @Mock private AuctionSessionRepository auctionSessionRepository;
  @Mock private AuctionItemRepository auctionItemRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private UserRepository userRepository;

  private AuctionService auctionService;

  @BeforeEach
  void setUp() {
    auctionService =
        new AuctionService(
            auctionSessionRepository, auctionItemRepository, categoryRepository, userRepository);
  }

  // =========================================================
  // HELPER — tạo CreateAuctionRequest hợp lệ
  // =========================================================

  private CreateAuctionRequest validCreateRequest() {
    return new CreateAuctionRequest(
        1L, // sellerId
        2, // categoryId
        "Đồng hồ cổ Seiko", // itemName
        "Đồng hồ Seiko vintage 1970, còn hoạt động tốt", // description
        new BigDecimal("500000"), // startingPrice
        LocalDateTime.now().plusHours(1), // startTime
        LocalDateTime.now().plusDays(3) // endTime
        );
  }

  // =========================================================
  // getAllAuctions() — Validate page / size
  // =========================================================

  @Nested
  @DisplayName("getAllAuctions() — Validate tham số")
  class GetAllAuctionsValidation {

    @Test
    @DisplayName("Ném AuctionException khi page âm")
    void getAllAuctions_negativePage_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAllAuctions(-1, 10))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Page must be >= 0");
    }

    @Test
    @DisplayName("Không ném exception khi page = 0 (biên hợp lệ)")
    void getAllAuctions_zeroPage_doesNotThrowValidationException() {
      // page=0 vượt qua validate, nhưng DbExecutor sẽ fail vì không có Hibernate
      // → chỉ verify không ném AuctionException với message validate
      assertThatThrownBy(() -> auctionService.getAllAuctions(0, 10))
          .isNotInstanceOf(AuctionException.class); // hoặc test với H2
    }

    @Test
    @DisplayName("Ném AuctionException khi size = 0")
    void getAllAuctions_zeroSize_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAllAuctions(0, 0))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Ném AuctionException khi size âm")
    void getAllAuctions_negativeSize_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAllAuctions(0, -5))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Ném AuctionException khi size > 100")
    void getAllAuctions_oversizedSize_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAllAuctions(0, 101))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Không ném AuctionException khi size = 100 (biên hợp lệ)")
    void getAllAuctions_maxSize_doesNotThrowValidationException() {
      assertThatThrownBy(() -> auctionService.getAllAuctions(0, 100))
          .isNotInstanceOf(AuctionException.class);
    }
  }

  // =========================================================
  // getAuctionDetail() — Validate auctionId
  // =========================================================

  @Nested
  @DisplayName("getAuctionDetail() — Validate auctionId")
  class GetAuctionDetailValidation {

    @Test
    @DisplayName("Ném AuctionException khi auctionId null")
    void getAuctionDetail_nullId_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAuctionDetail(null))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid auction id");
    }

    @Test
    @DisplayName("Ném AuctionException khi auctionId = 0")
    void getAuctionDetail_zeroId_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAuctionDetail(0L))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid auction id");
    }

    @Test
    @DisplayName("Ném AuctionException khi auctionId âm")
    void getAuctionDetail_negativeId_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.getAuctionDetail(-1L))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid auction id");
    }
  }

  // =========================================================
  // createAuction() — Validate CreateAuctionRequest
  // =========================================================

  @Nested
  @DisplayName("createAuction() — Validate request")
  class CreateAuctionValidation {

    @Test
    @DisplayName("Ném AuctionException khi request null")
    void createAuction_nullRequest_throwsAuctionException() {
      assertThatThrownBy(() -> auctionService.createAuction(null))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Create auction request is required");
    }

    @Test
    @DisplayName("Ném AuctionException khi sellerId null")
    void createAuction_nullSellerId_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setSellerId(null);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid seller id");
    }

    @Test
    @DisplayName("Ném AuctionException khi sellerId <= 0")
    void createAuction_zeroSellerId_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setSellerId(0L);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid seller id");
    }

    @Test
    @DisplayName("Ném AuctionException khi categoryId null")
    void createAuction_nullCategoryId_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setCategoryId(null);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Invalid category id");
    }

    @Test
    @DisplayName("Ném AuctionException khi itemName trống")
    void createAuction_blankItemName_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setItemName("   ");

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Item name is required");
    }

    @Test
    @DisplayName("Ném AuctionException khi description trống")
    void createAuction_blankDescription_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setDescription("");

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Description is required");
    }

    @Test
    @DisplayName("Ném AuctionException khi startingPrice null")
    void createAuction_nullStartingPrice_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setStartingPrice(null);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Starting price must be greater than 0");
    }

    @Test
    @DisplayName("Ném AuctionException khi startingPrice = 0")
    void createAuction_zeroStartingPrice_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setStartingPrice(BigDecimal.ZERO);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Starting price must be greater than 0");
    }

    @Test
    @DisplayName("Ném AuctionException khi startingPrice âm")
    void createAuction_negativeStartingPrice_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setStartingPrice(new BigDecimal("-100"));

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Starting price must be greater than 0");
    }

    @Test
    @DisplayName("Ném AuctionException khi startTime null")
    void createAuction_nullStartTime_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setStartTime(null);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("Start time is required");
    }

    @Test
    @DisplayName("Ném AuctionException khi endTime null")
    void createAuction_nullEndTime_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setEndTime(null);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("End time is required");
    }

    @Test
    @DisplayName("Ném AuctionException khi endTime trước startTime")
    void createAuction_endTimeBeforeStartTime_throwsAuctionException() {
      CreateAuctionRequest req = validCreateRequest();
      req.setStartTime(LocalDateTime.now().plusDays(5));
      req.setEndTime(LocalDateTime.now().plusDays(1)); // endTime < startTime

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("End time must be after start time");
    }

    @Test
    @DisplayName("Ném AuctionException khi endTime bằng startTime (không phải sau)")
    void createAuction_endTimeEqualsStartTime_throwsAuctionException() {
      LocalDateTime same = LocalDateTime.now().plusDays(2);
      CreateAuctionRequest req = validCreateRequest();
      req.setStartTime(same);
      req.setEndTime(same);

      assertThatThrownBy(() -> auctionService.createAuction(req))
          .isInstanceOf(AuctionException.class)
          .hasMessageContaining("End time must be after start time");
    }
  }
}
