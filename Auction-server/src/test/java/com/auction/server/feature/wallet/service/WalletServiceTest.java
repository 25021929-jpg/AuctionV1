package com.auction.server.feature.wallet.service;

import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho WalletService.
 * Chỉ test validate chạy trước DbExecutor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Tests")
class WalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
                userRepository,
                walletTransactionRepository
        );
    }

    @Nested
    @DisplayName("getSummary() validation")
    class GetSummaryValidation {

        @Test
        void nullUserId_throwsException() {
            assertThatThrownBy(() -> walletService.getSummary(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }

        @Test
        void zeroUserId_throwsException() {
            assertThatThrownBy(() -> walletService.getSummary(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }

        @Test
        void negativeUserId_throwsException() {
            assertThatThrownBy(() -> walletService.getSummary(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }
    }

    @Nested
    @DisplayName("deposit() validation")
    class DepositValidation {

        @Test
        void nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    walletService.deposit(null, BigDecimal.valueOf(1000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }

        @Test
        void nullAmount_throwsException() {
            assertThatThrownBy(() ->
                    walletService.deposit(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Số tiền nạp không được để trống");
        }

        @Test
        void zeroAmount_throwsException() {
            assertThatThrownBy(() ->
                    walletService.deposit(1L, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Số tiền nạp phải lớn hơn 0");
        }

        @Test
        void negativeAmount_throwsException() {
            assertThatThrownBy(() ->
                    walletService.deposit(1L, BigDecimal.valueOf(-5000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Số tiền nạp phải lớn hơn 0");
        }

        @Test
        void decimalAmount_throwsException() {
            assertThatThrownBy(() ->
                    walletService.deposit(1L, new BigDecimal("1000.50")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("không nhập tiền lẻ");
        }
    }

    @Nested
    @DisplayName("getTransactions() validation")
    class GetTransactionsValidation {

        @Test
        void nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    walletService.getTransactions(null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }

        @Test
        void negativeUserId_throwsException() {
            assertThatThrownBy(() ->
                    walletService.getTransactions(-10L, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId không hợp lệ");
        }
    }
}
