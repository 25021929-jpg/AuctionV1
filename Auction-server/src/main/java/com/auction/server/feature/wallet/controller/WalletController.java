package com.auction.server.feature.wallet.controller;

import com.auction.server.feature.wallet.service.WalletService;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.wallet.DepositRequest;
import com.auction.shared.dto.wallet.WalletSummaryDto;
import com.auction.shared.dto.wallet.WalletTransactionDto;
import com.auction.shared.dto.wallet.WalletTransactionHistoryRequest;
import com.auction.shared.protocol.JsonSupport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;

public class WalletController {
  private final WalletService walletService;
  private final Gson gson = JsonSupport.createGson();

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  public Response<WalletSummaryDto> getSummary(String requestBody) {
    try {
      Long userId = extractUserId(requestBody);
      return Response.success("Wallet summary loaded", walletService.getSummary(userId));
    } catch (Exception e) {
      return Response.fail(e.getMessage());
    }
  }

  public Response<WalletSummaryDto> deposit(String requestBody) {
    try {
      DepositRequest request = gson.fromJson(requestBody, DepositRequest.class);
      WalletSummaryDto summary =
          walletService.deposit(
              request == null ? null : request.getUserId(),
              request == null ? null : request.getAmount());
      return Response.success("Deposit successful", summary);
    } catch (Exception e) {
      return Response.fail(e.getMessage());
    }
  }

  public Response<List<WalletTransactionDto>> getTransactions(String requestBody) {
    try {
      WalletTransactionHistoryRequest request =
          gson.fromJson(requestBody, WalletTransactionHistoryRequest.class);
      Long userId = request == null ? null : request.getUserId();
      int limit = request == null ? 50 : request.getLimit();
      return Response.success(
          "Wallet transactions loaded", walletService.getTransactions(userId, limit));
    } catch (Exception e) {
      return Response.fail(e.getMessage());
    }
  }

  private Long extractUserId(String requestBody) {
    JsonObject obj = gson.fromJson(requestBody, JsonObject.class);
    if (obj == null || !obj.has("userId") || obj.get("userId").isJsonNull()) {
      return null;
    }
    return obj.get("userId").getAsLong();
  }
}
