package com.auction.server.feature.seller.service;

import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.seller.SellerException;
import com.auction.shared.dto.seller.CreateSellerItemRequest;
import com.auction.shared.dto.seller.DeleteSellerItemRequest;
import com.auction.shared.dto.seller.UpdateSellerItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho SellerService.
 *
 * Giống AuctionService/BidService, validate nằm trước DbExecutor
 * → test validate không cần Hibernate session.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerService Tests")
class SellerServiceTest {

    @Mock private AuctionSessionRepository auctionSessionRepository;
    @Mock private AuctionItemRepository    auctionItemRepository;
    @Mock private CategoryRepository       categoryRepository;
    @Mock private UserRepository           userRepository;

    private SellerService sellerService;

    @BeforeEach
    void setUp() {
        sellerService = new SellerService(
                auctionSessionRepository,
                auctionItemRepository,
                categoryRepository,
                userRepository
        );
    }

    // =========================================================
    // HELPER
    // =========================================================

    private CreateSellerItemRequest validCreateRequest() {
        CreateSellerItemRequest req = new CreateSellerItemRequest();
        req.setSellerId(1L);
        req.setCategoryId(2L);
        req.setName("Đồng hồ cổ");
        req.setDescription("Đồng hồ Seiko 1970 còn hoạt động tốt");
        req.setStartPrice(new BigDecimal("500000"));
        req.setStartTime(LocalDateTime.now().plusHours(1));
        req.setEndTime(LocalDateTime.now().plusDays(3));
        return req;
    }

    private UpdateSellerItemRequest validUpdateRequest() {
        UpdateSellerItemRequest req = new UpdateSellerItemRequest();
        req.setSellerId(1L);
        req.setCategoryId(2L);
        req.setName("Đồng hồ cổ updated");
        req.setDescription("Mô tả mới đầy đủ hơn");
        req.setStartPrice(new BigDecimal("600000"));
        req.setStartTime(LocalDateTime.now().plusHours(1));
        req.setEndTime(LocalDateTime.now().plusDays(3));
        req.setAuctionId(10L); // cần ít nhất itemId hoặc auctionId > 0
        return req;
    }

    // =========================================================
    // listMyItems() — Validate
    // =========================================================

    @Nested
    @DisplayName("listMyItems() — Validate")
    class ListMyItemsValidation {

        @Test
        @DisplayName("Ném SellerException khi sellerId = 0")
        void listMyItems_zeroSellerId_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.listMyItems(0L, 0, 10))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Seller id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi sellerId âm")
        void listMyItems_negativeSellerId_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.listMyItems(-1L, 0, 10))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Seller id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi page âm")
        void listMyItems_negativePage_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.listMyItems(1L, -1, 10))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Page must be >= 0");
        }

        @Test
        @DisplayName("Ném SellerException khi size = 0")
        void listMyItems_zeroSize_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.listMyItems(1L, 0, 0))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Size must be between 1 and 100");
        }

        @Test
        @DisplayName("Ném SellerException khi size > 100")
        void listMyItems_oversizedSize_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.listMyItems(1L, 0, 101))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Size must be between 1 and 100");
        }
    }

    // =========================================================
    // createItem() — Validate
    // =========================================================

    @Nested
    @DisplayName("createItem() — Validate")
    class CreateItemValidation {

        @Test
        @DisplayName("Ném SellerException khi request null")
        void createItem_nullRequest_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.createItem(null))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Create item request is required");
        }

        @Test
        @DisplayName("Ném SellerException khi sellerId = 0")
        void createItem_zeroSellerId_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setSellerId(0L);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Seller id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi categoryId = 0")
        void createItem_zeroCategoryId_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setCategoryId(0L);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Category id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi name trống")
        void createItem_blankName_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setName("   ");

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Item name is required");
        }

        @Test
        @DisplayName("Ném SellerException khi name null")
        void createItem_nullName_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setName(null);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Item name is required");
        }

        @Test
        @DisplayName("Ném SellerException khi description trống")
        void createItem_blankDescription_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setDescription("");

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Description is required");
        }

        @Test
        @DisplayName("Ném SellerException khi startPrice null")
        void createItem_nullStartPrice_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartPrice(null);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Start price must be greater than 0");
        }

        @Test
        @DisplayName("Ném SellerException khi startPrice = 0")
        void createItem_zeroStartPrice_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartPrice(BigDecimal.ZERO);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Start price must be greater than 0");
        }

        @Test
        @DisplayName("Ném SellerException khi startPrice âm")
        void createItem_negativeStartPrice_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartPrice(new BigDecimal("-1000"));

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Start price must be greater than 0");
        }

        @Test
        @DisplayName("Ném SellerException khi startTime null")
        void createItem_nullStartTime_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartTime(null);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Start time and end time are required");
        }

        @Test
        @DisplayName("Ném SellerException khi endTime null")
        void createItem_nullEndTime_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setEndTime(null);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Start time and end time are required");
        }

        @Test
        @DisplayName("Ném SellerException khi endTime trước startTime")
        void createItem_endTimeBeforeStartTime_throwsSellerException() {
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartTime(LocalDateTime.now().plusDays(5));
            req.setEndTime(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("End time must be after start time");
        }

        @Test
        @DisplayName("Ném SellerException khi endTime bằng startTime")
        void createItem_endTimeEqualsStartTime_throwsSellerException() {
            LocalDateTime same = LocalDateTime.now().plusDays(2);
            CreateSellerItemRequest req = validCreateRequest();
            req.setStartTime(same);
            req.setEndTime(same);

            assertThatThrownBy(() -> sellerService.createItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("End time must be after start time");
        }
    }

    // =========================================================
    // updateItem() — Validate
    // =========================================================

    @Nested
    @DisplayName("updateItem() — Validate")
    class UpdateItemValidation {

        @Test
        @DisplayName("Ném SellerException khi itemId và auctionId đều <= 0")
        void updateItem_noItemOrAuctionId_throwsSellerException() {
            UpdateSellerItemRequest req = validUpdateRequest();
            req.setItemId(0L);
            req.setAuctionId(0L);

            assertThatThrownBy(() -> sellerService.updateItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Item id or auction id is required");
        }

        @Test
        @DisplayName("Không ném validate exception khi chỉ có auctionId > 0")
        void updateItem_auctionIdOnly_passesValidation() {
            UpdateSellerItemRequest req = validUpdateRequest();
            req.setItemId(0L);
            req.setAuctionId(5L);

            // Validate pass, nhưng DbExecutor sẽ fail — chỉ verify không ném SellerException validate
            assertThatThrownBy(() -> sellerService.updateItem(req))
                    .isNotInstanceOf(SellerException.class);
        }

        @Test
        @DisplayName("Kế thừa validate của createItem — sellerId = 0 vẫn bị bắt")
        void updateItem_zeroSellerId_throwsSellerException() {
            UpdateSellerItemRequest req = validUpdateRequest();
            req.setSellerId(0L);

            assertThatThrownBy(() -> sellerService.updateItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Seller id is required");
        }
    }

    // =========================================================
    // deleteItem() — Validate
    // =========================================================

    @Nested
    @DisplayName("deleteItem() — Validate")
    class DeleteItemValidation {

        @Test
        @DisplayName("Ném SellerException khi request null")
        void deleteItem_nullRequest_throwsSellerException() {
            assertThatThrownBy(() -> sellerService.deleteItem(null))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Item id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi itemId = 0")
        void deleteItem_zeroItemId_throwsSellerException() {
            DeleteSellerItemRequest req = new DeleteSellerItemRequest(0L, null, 1L);

            assertThatThrownBy(() -> sellerService.deleteItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Item id is required");
        }

        @Test
        @DisplayName("Ném SellerException khi sellerId = 0")
        void deleteItem_zeroSellerId_throwsSellerException() {
            DeleteSellerItemRequest req = new DeleteSellerItemRequest(1L, null, 0L);

            assertThatThrownBy(() -> sellerService.deleteItem(req))
                    .isInstanceOf(SellerException.class)
                    .hasMessageContaining("Seller id is required");
        }
    }
}