package com.auction.client.feature.auction.mapper;

import com.auction.shared.dto.auction.AuctionDetailDto;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import com.google.gson.JsonObject;
import com.auction.shared.domain.AuctionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDtoMapperTest {

    @Test
    void toSummary_shouldReadAlternativeKeys() {
        JsonObject o = new JsonObject();
        o.addProperty("id", 99);
        o.addProperty("title", "Macbook");
        o.addProperty("highestBid", 1500);
        o.addProperty("state", "RUNNING");
        o.addProperty("endAt", "2026-05-25T12:00:00");

        AuctionSummaryDto dto = AuctionDtoMapper.toSummary(o);
        assertEquals(99, dto.getAuctionId());
        assertEquals("Macbook", dto.getItemName());
        assertEquals(1500.0, dto.getCurrentPrice().doubleValue());
        assertEquals(AuctionStatus.RUNNING, dto.getStatus());
        assertNotNull(dto.getEndTime());
    }

    @Test
    void toDetail_shouldFallbackToProvidedAuctionId() {
        JsonObject o = new JsonObject();
        o.addProperty("name", "Art");
        o.addProperty("currentBid", 200);

        AuctionDetailDto dto = AuctionDtoMapper.toDetail(o, 123);
        assertEquals(123, dto.getAuctionId());
        assertEquals("Art", dto.getItemName());
        assertEquals(200.0, dto.getCurrentPrice().doubleValue());
    }

    @Test
    void toSummary_missingFields_shouldNotCrashAndUseDefaults() {
        JsonObject o = new JsonObject();

        AuctionSummaryDto dto = AuctionDtoMapper.toSummary(o);
        assertNotNull(dto);
        assertEquals(0, dto.getAuctionId());
        assertEquals("", dto.getItemName());
        assertEquals(0.0, dto.getCurrentPrice().doubleValue());
        assertNull(dto.getStatus());
    }

    @Test
    void toDetail_missingFields_shouldNotCrashAndUseDefaults() {
        JsonObject o = new JsonObject();

        AuctionDetailDto dto = AuctionDtoMapper.toDetail(o, 7);
        assertNotNull(dto);
        assertEquals(7, dto.getAuctionId());
        assertEquals("", dto.getItemName());
        assertEquals(0.0, dto.getCurrentPrice().doubleValue());
    }
}
