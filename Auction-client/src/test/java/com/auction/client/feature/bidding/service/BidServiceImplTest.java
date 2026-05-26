package com.auction.client.feature.bidding.service;

import com.auction.shared.protocol.ActionConstants;
import com.auction.client.core.error.ApiException;
import com.auction.client.core.session.UserSession;
import com.auction.client.testsupport.FakeServerCommunicator;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.UserInfo;
import com.auction.shared.dto.auction.AuctionIdRequest;
import com.auction.shared.dto.bidding.PlaceBidRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidServiceImplTest {

    @AfterEach
    void tearDown() {
        UserSession.getInstance().clear();
    }

    @Test
    void placeBidUsesLoggedInUserIdAndBigDecimalAmount() throws IOException {
        UserSession.getInstance().setCurrentUser(new UserInfo(
                7L, "Bidder A", "biddera", "a@example.com", "0900000000", "2000-01-01", "BIDDER"
        ));
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        communicator.setNextResponse(Response.success("Accepted", null));

        BidServiceImpl service = new BidServiceImpl(communicator);
        boolean placed = service.placeBid(12L, new BigDecimal("1500000.50"));

        assertTrue(placed);
        assertEquals(ActionConstants.BID_PLACE_BID, communicator.lastCall().action());
        PlaceBidRequest request = assertInstanceOf(PlaceBidRequest.class, communicator.lastCall().body());
        assertEquals(12L, request.getAuctionId());
        assertEquals(7L, request.getBidderId());
        assertEquals(new BigDecimal("1500000.50"), request.getAmount());
    }

    @Test
    void placeBidFailsWhenUserIsNotLoggedIn() {
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        BidServiceImpl service = new BidServiceImpl(communicator);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.placeBid(12L, new BigDecimal("1500000")));

        assertEquals(ActionConstants.BID_PLACE_BID, exception.getAction());
        assertTrue(communicator.calls().isEmpty());
    }

    @Test
    void subscribeAuctionSendsAuctionIdRequest() throws IOException {
        FakeServerCommunicator communicator = new FakeServerCommunicator();
        communicator.setNextResponse(Response.success("OK", null));

        BidServiceImpl service = new BidServiceImpl(communicator);
        service.subscribeAuction(99L);

        assertEquals(ActionConstants.AUCTION_SUBSCRIBE, communicator.lastCall().action());
        AuctionIdRequest request = assertInstanceOf(AuctionIdRequest.class, communicator.lastCall().body());
        assertEquals(99L, request.auctionId());
    }
}
