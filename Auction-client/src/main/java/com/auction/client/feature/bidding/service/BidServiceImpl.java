package com.auction.client.feature.bidding.service;

import com.auction.client.core.error.ApiException;
import com.auction.client.core.error.ResponseUtils;
import com.auction.client.core.session.UserSession;
import com.auction.client.network.ServerCommunicator;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.auction.AuctionIdRequest;
import com.auction.shared.dto.bidding.BidHistoryRequest;
import com.auction.shared.dto.bidding.BidResultDto;
import com.auction.shared.dto.bidding.PlaceBidRequest;
import com.auction.shared.protocol.ActionConstants;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** BidService implementation. */
public class BidServiceImpl implements BidService {

  private final ServerCommunicator communicator;

  public BidServiceImpl() {
    this(SocketClient.getInstance());
  }

  public BidServiceImpl(ServerCommunicator communicator) {
    this.communicator = communicator;
  }

  @Override
  public void subscribeAuction(long auctionId) throws IOException {
    AuctionIdRequest request = new AuctionIdRequest(auctionId);
    communicator.send(ActionConstants.AUCTION_SUBSCRIBE, request, Void.class);
  }

  @Override
  public void unsubscribeAuction(long auctionId) throws IOException {
    AuctionIdRequest request = new AuctionIdRequest(auctionId);
    try {
      communicator.send(ActionConstants.AUCTION_UNSUBSCRIBE, request, Void.class);
    } catch (IOException ignored) {
      // best-effort: khi đang rời màn hình thì không làm hỏng navigation.
    }
  }

  @Override
  public boolean placeBid(long auctionId, BigDecimal amount) throws IOException {
    Long bidderId = UserSession.getInstance().getUserId();
    if (bidderId == null || bidderId <= 0) {
      throw new ApiException(
          ActionConstants.BID_PLACE_BID,
          "Bạn cần đăng nhập bằng tài khoản hợp lệ trước khi đặt giá.");
    }

    PlaceBidRequest request = new PlaceBidRequest(auctionId, bidderId, amount);
    Response<Void> response = communicator.send(ActionConstants.BID_PLACE_BID, request, Void.class);
    ResponseUtils.unwrap(ActionConstants.BID_PLACE_BID, response);
    return true;
  }

  @Override
  public List<BidResultDto> getBidHistory(long auctionId, int limit) throws IOException {
    BidHistoryRequest request = new BidHistoryRequest(auctionId, limit);
    Response<BidResultDto[]> response =
        communicator.send(ActionConstants.BID_GET_HISTORY, request, BidResultDto[].class);
    BidResultDto[] history = ResponseUtils.unwrap(ActionConstants.BID_GET_HISTORY, response);
    return history == null ? List.of() : Arrays.asList(history);
  }
}
