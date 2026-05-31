package com.auction.server.feature.wallet.controller;

import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.wallet.repository.WalletTransactionRepository;
import com.auction.server.feature.wallet.service.WalletService;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.wallet.WalletSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTest {

    private WalletController controller;

    @BeforeEach
    void setUp() {

        UserRepository userRepository =
                mock(UserRepository.class);

        WalletTransactionRepository txRepository =
                mock(WalletTransactionRepository.class);

        WalletService walletService =
                new WalletService(
                        userRepository,
                        txRepository
                );

        controller = new WalletController(walletService);
    }

    @Test
    void getSummary_invalidUserId() {

        Response<WalletSummaryDto> response =
                controller.getSummary("{}");

        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    void getSummary_nullUserId() {

        Response<WalletSummaryDto> response =
                controller.getSummary("{\"userId\":null}");

        assertFalse(response.isSuccess());
    }
}