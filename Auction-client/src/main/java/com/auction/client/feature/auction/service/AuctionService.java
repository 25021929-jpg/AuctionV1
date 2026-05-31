package com.auction.client.feature.auction.service;

import com.auction.shared.dto.auction.AuctionDetailDto;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import java.io.IOException;
import java.util.List;

public interface AuctionService {

  List<AuctionSummaryDto> fetchAuctions() throws IOException;

  AuctionDetailDto fetchAuctionDetail(long auctionId) throws IOException;
}
