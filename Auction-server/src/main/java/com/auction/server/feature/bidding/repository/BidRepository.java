package com.auction.server.feature.bidding.repository;

import com.auction.server.entity.Bid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BidRepository {
  List<Bid> findTopByAuction(Long auctionId, int limit);

  Optional<Bid> findWinningBid(Long auctionId);

  List<Bid> findByBidder(Long bidderId, int page, int size);

  boolean existsByAuctionAndBidder(Long auctionId, Long bidderId);

  List<Long> findDistinctBidderIdsByAuction(Long auctionId);

  long countByAuction(Long auctionId);

  Optional<BigDecimal> findMaxAmount(Long auctionId);

  Bid save(Bid bid);

  void clearWinningBids(Long auctionId);
}
