package com.auction.server.feature.seller.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.AuctionItem;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.Category;
import com.auction.server.entity.User;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.shared.dto.seller.CreateSellerItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test riêng cho logic initialAuctionStatus() trong SellerService.
 *
 * Logic cần test:
 *   startTime trong tương lai  → SCHEDULED
 *   startTime trong quá khứ   → ACTIVE
 *
 * Vì initialAuctionStatus() là private, test gián tiếp qua createItem():
 *   Capture AuctionSession được truyền vào auctionSessionRepository.save()
 *   → kiểm tra field status của nó.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerService — initialAuctionStatus() Tests")
class SellerServiceAuctionStatusTest {

    @Mock private AuctionSessionRepository auctionSessionRepository;
    @Mock private AuctionItemRepository    auctionItemRepository;
    @Mock private CategoryRepository       categoryRepository;
    @Mock private UserRepository           userRepository;

    private SellerService sellerService;

    @BeforeEach
    void setUp() {
        sellerService = new SellerService(
                auctionSessionRepository, auctionItemRepository,
                categoryRepository, userRepository);
    }

    // ── helper ───────────────────────────────────────────────

    private MockedStatic<DbExecutor> mockRunAndReturn() {
        MockedStatic<DbExecutor> m = mockStatic(DbExecutor.class);
        m.when(() -> DbExecutor.runAndReturn(any(Supplier.class)))
         .thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());
        return m;
    }

    /** Tạo request với startTime tùy chỉnh */
    private CreateSellerItemRequest requestWithStartTime(LocalDateTime startTime) {
        CreateSellerItemRequest req = new CreateSellerItemRequest();
        req.setSellerId(1L);
        req.setCategoryId(2L);
        req.setName("Test Item");
        req.setDescription("Mô tả test");
        req.setStartPrice(new BigDecimal("100000"));
        req.setStartTime(startTime);
        req.setEndTime(startTime.plusDays(3)); // luôn sau startTime
        return req;
    }

    /** Setup mock DB trả về category + seller + savedItem */
    private AuctionSession setupMocksAndCaptureSession() {
        Category cat = new Category(); cat.setCategoryId(2);
        when(categoryRepository.findById(2)).thenReturn(Optional.of(cat));
        when(categoryRepository.getReference(2)).thenReturn(cat);

        User seller = new User(); seller.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(userRepository.getReference(1L)).thenReturn(seller);

        AuctionItem savedItem = new AuctionItem();
        savedItem.setItemId(10L);
        savedItem.setCategory(cat);
        savedItem.setSeller(seller);
        when(auctionItemRepository.save(any())).thenReturn(savedItem);

        // Capture AuctionSession được save để kiểm tra status
        ArgumentCaptor<AuctionSession> captor = ArgumentCaptor.forClass(AuctionSession.class);
        when(auctionSessionRepository.save(captor.capture())).thenAnswer(inv -> {
            AuctionSession s = inv.getArgument(0);
            s.setAuctionId(99L);

            // set item cho toSummaryDto không NPE
            AuctionItem item = new AuctionItem();
            item.setItemId(10L);
            item.setCategory(cat);
            item.setSeller(seller);
            s.setItem(item);
            return s;
        });

        return null; // giá trị thực lấy từ captor sau khi gọi service
    }

    // =========================================================
    // initialAuctionStatus() — via createItem()
    // =========================================================

    @Test
    @DisplayName("SCHEDULED khi startTime trong tương lai (1 giờ sau)")
    void createItem_futureStartTime_statusIsScheduled() {
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();

            LocalDateTime futureStart = LocalDateTime.now().plusHours(1);
            sellerService.createItem(requestWithStartTime(futureStart));

            // Capture AuctionSession được truyền vào save()
            ArgumentCaptor<AuctionSession> captor = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.SCHEDULED);
        }
    }

    @Test
    @DisplayName("SCHEDULED khi startTime trong tương lai xa (7 ngày)")
    void createItem_farFutureStartTime_statusIsScheduled() {
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();

            LocalDateTime farFuture = LocalDateTime.now().plusDays(7);
            sellerService.createItem(requestWithStartTime(farFuture));

            ArgumentCaptor<AuctionSession> captor = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.SCHEDULED);
        }
    }

    @Test
    @DisplayName("ACTIVE khi startTime trong quá khứ (1 giờ trước)")
    void createItem_pastStartTime_statusIsActive() {
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();

            LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
            // endTime phải sau now để vượt validate "End time must be after start time"
            CreateSellerItemRequest req = requestWithStartTime(pastStart);
            req.setEndTime(LocalDateTime.now().plusDays(1));

            sellerService.createItem(req);

            ArgumentCaptor<AuctionSession> captor = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.ACTIVE);
        }
    }

    @Test
    @DisplayName("ACTIVE khi startTime trong quá khứ xa (30 ngày)")
    void createItem_farPastStartTime_statusIsActive() {
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();

            CreateSellerItemRequest req = requestWithStartTime(LocalDateTime.now().minusDays(30));
            req.setEndTime(LocalDateTime.now().plusDays(1));

            sellerService.createItem(req);

            ArgumentCaptor<AuctionSession> captor = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.ACTIVE);
        }
    }

    @Test
    @DisplayName("Hai lần tạo: future → SCHEDULED, past → ACTIVE (kiểm tra không bị cache)")
    void createItem_bothStatuses_independentOfEachOther() {
        // Test 1: future → SCHEDULED
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();
            sellerService.createItem(requestWithStartTime(LocalDateTime.now().plusHours(1)));

            ArgumentCaptor<AuctionSession> cap1 = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(cap1.capture());
            assertThat(cap1.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.SCHEDULED);
        }

        // Reset mocks
        reset(auctionSessionRepository, auctionItemRepository, categoryRepository, userRepository);

        // Test 2: past → ACTIVE
        try (MockedStatic<DbExecutor> db = mockRunAndReturn()) {
            setupMocksAndCaptureSession();
            CreateSellerItemRequest pastReq = requestWithStartTime(LocalDateTime.now().minusHours(1));
            pastReq.setEndTime(LocalDateTime.now().plusDays(1));
            sellerService.createItem(pastReq);

            ArgumentCaptor<AuctionSession> cap2 = ArgumentCaptor.forClass(AuctionSession.class);
            verify(auctionSessionRepository).save(cap2.capture());
            assertThat(cap2.getValue().getStatus())
                    .isEqualTo(AuctionSession.AuctionStatus.ACTIVE);
        }
    }
}
