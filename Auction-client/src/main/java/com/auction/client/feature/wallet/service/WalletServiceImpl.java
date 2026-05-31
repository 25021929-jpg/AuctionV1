package com.auction.client.feature.wallet.service;

import com.auction.client.core.error.ApiException;
import com.auction.client.core.error.ResponseUtils;
import com.auction.client.core.session.UserSession;
import com.auction.client.network.ServerCommunicator;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.wallet.DepositRequest;
import com.auction.shared.dto.wallet.WalletSummaryDto;
import com.auction.shared.dto.wallet.WalletTransactionDto;
import com.auction.shared.dto.wallet.WalletTransactionHistoryRequest;
import com.auction.shared.protocol.ActionConstants;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class WalletServiceImpl implements WalletService {
    private final ServerCommunicator communicator;

    public WalletServiceImpl() {
        this(SocketClient.getInstance());
    }

    public WalletServiceImpl(ServerCommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public WalletSummaryDto getSummary() throws IOException {
        Long userId = requireUserId();
        WalletTransactionHistoryRequest request = new WalletTransactionHistoryRequest(userId, 1);
        Response<WalletSummaryDto> response = communicator.send(ActionConstants.WALLET_GET_SUMMARY, request, WalletSummaryDto.class);
        WalletSummaryDto summary = ResponseUtils.unwrap(ActionConstants.WALLET_GET_SUMMARY, response);
        if (summary != null) {
            UserSession.getInstance().updateBalance(summary.getBalance());
        }
        return summary;
    }

    @Override
    public WalletSummaryDto deposit(BigDecimal amount) throws IOException {
        Long userId = requireUserId();
        DepositRequest request = new DepositRequest(userId, amount);
        Response<WalletSummaryDto> response = communicator.send(ActionConstants.WALLET_DEPOSIT, request, WalletSummaryDto.class);
        WalletSummaryDto summary = ResponseUtils.unwrap(ActionConstants.WALLET_DEPOSIT, response);
        if (summary != null) {
            UserSession.getInstance().updateBalance(summary.getBalance());
        }
        return summary;
    }

    @Override
    public List<WalletTransactionDto> getTransactions(int limit) throws IOException {
        Long userId = requireUserId();
        WalletTransactionHistoryRequest request = new WalletTransactionHistoryRequest(userId, limit);
        Response<WalletTransactionDto[]> response = communicator.send(
                ActionConstants.WALLET_GET_TRANSACTIONS,
                request,
                WalletTransactionDto[].class
        );
        WalletTransactionDto[] rows = ResponseUtils.unwrap(ActionConstants.WALLET_GET_TRANSACTIONS, response);
        return rows == null ? List.of() : Arrays.asList(rows);
    }

    private Long requireUserId() throws ApiException {
        Long userId = UserSession.getInstance().getUserId();
        if (userId == null || userId <= 0) {
            throw new ApiException("WALLET", "Bạn cần đăng nhập trước khi dùng ví.");
        }
        return userId;
    }
}
