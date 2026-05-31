package com.auction.server.feature;

import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.seller.SellerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test coverage cho 4 custom exception class.
 * Verify: extends RuntimeException, constructor message, constructor với cause.
 */
@DisplayName("Custom Exception Tests")
class ExceptionTest {

    // =========================================================
    // AuthException
    // =========================================================

    @Nested
    @DisplayName("AuthException")
    class AuthExceptionTest {

        @Test
        @DisplayName("extends RuntimeException")
        void isRuntimeException() {
            assertThat(new AuthException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("constructor(message) gán đúng message")
        void constructor_message() {
            AuthException ex = new AuthException("Invalid credentials");
            assertThat(ex.getMessage()).isEqualTo("Invalid credentials");
        }

        @Test
        @DisplayName("getMessage() không null")
        void getMessage_notNull() {
            assertThat(new AuthException("x").getMessage()).isNotNull();
        }
    }

    // =========================================================
    // AuctionException
    // =========================================================

    @Nested
    @DisplayName("AuctionException")
    class AuctionExceptionTest {

        @Test
        @DisplayName("extends RuntimeException")
        void isRuntimeException() {
            assertThat(new AuctionException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("constructor(message) gán đúng message")
        void constructor_message() {
            AuctionException ex = new AuctionException("Auction not found");
            assertThat(ex.getMessage()).isEqualTo("Auction not found");
        }

        @Test
        @DisplayName("constructor(message, cause) gán đúng message và cause")
        void constructor_messageAndCause() {
            Throwable cause = new IllegalStateException("root cause");
            AuctionException ex = new AuctionException("Auction error", cause);

            assertThat(ex.getMessage()).isEqualTo("Auction error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("cause được wrap đúng")
        void cause_isWrapped() {
            RuntimeException root = new RuntimeException("DB error");
            AuctionException ex = new AuctionException("Wrapped", root);
            assertThat(ex.getCause().getMessage()).isEqualTo("DB error");
        }
    }

    // =========================================================
    // BidException
    // =========================================================

    @Nested
    @DisplayName("BidException")
    class BidExceptionTest {

        @Test
        @DisplayName("extends RuntimeException")
        void isRuntimeException() {
            assertThat(new BidException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("constructor(message) gán đúng message")
        void constructor_message() {
            BidException ex = new BidException("Giá đặt không hợp lệ");
            assertThat(ex.getMessage()).isEqualTo("Giá đặt không hợp lệ");
        }

        @Test
        @DisplayName("constructor(message, cause) gán đúng cả hai")
        void constructor_messageAndCause() {
            Throwable cause = new RuntimeException("lock timeout");
            BidException ex = new BidException("Bid failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Bid failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("là unchecked — không cần khai báo throws")
        void isUnchecked() {
            // Nếu compile được mà không có throws → unchecked ✓
            Runnable r = () -> { throw new BidException("test"); };
            assertThat(r).isNotNull();
        }
    }

    // =========================================================
    // SellerException
    // =========================================================

    @Nested
    @DisplayName("SellerException")
    class SellerExceptionTest {

        @Test
        @DisplayName("extends RuntimeException")
        void isRuntimeException() {
            assertThat(new SellerException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("constructor(message) gán đúng message")
        void constructor_message() {
            SellerException ex = new SellerException("Item not found");
            assertThat(ex.getMessage()).isEqualTo("Item not found");
        }

        @Test
        @DisplayName("getMessage() trả về đúng chuỗi đã truyền")
        void getMessage_returnsPassedString() {
            String msg = "Cannot delete item with active bids";
            assertThat(new SellerException(msg).getMessage()).isEqualTo(msg);
        }
    }
}
