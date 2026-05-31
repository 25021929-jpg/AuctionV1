package com.auction.client.feature.bidding.service;

import com.auction.shared.dto.bidding.BidResultDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service cho nghiệp vụ đặt giá phía client.
 *
 * <p>Server vẫn là nơi kiểm tra cuối cùng để đảm bảo concurrency.</p>
 */
public interface BidService {

    void subscribeAuction(long auctionId) throws IOException;

    void unsubscribeAuction(long auctionId) throws IOException;

    boolean placeBid(long auctionId, BigDecimal amount) throws IOException;

    List<BidResultDto> getBidHistory(long auctionId, int limit) throws IOException;
}
