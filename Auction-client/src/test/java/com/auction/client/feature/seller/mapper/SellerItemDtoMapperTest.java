package com.auction.client.feature.seller.mapper;

import com.auction.shared.dto.seller.SellerItemDto;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerItemDtoMapperTest {

    @Test
    void toDto_shouldMapCommonFields() {
        JsonObject o = new JsonObject();
        o.addProperty("itemId", 7);
        o.addProperty("itemName", "Keyboard");
        o.addProperty("startingPrice", 100);
        o.addProperty("currentPrice", 120);
        o.addProperty("status", "OPEN");

        SellerItemDto dto = SellerItemDtoMapper.toDto(o);
        assertEquals(7, dto.getItemId());
        assertEquals("Keyboard", dto.getName());
        assertEquals(100.0, dto.getStartPrice().doubleValue());
        assertEquals(120.0, dto.getCurrentPrice().doubleValue());
        assertEquals("OPEN", dto.getStatus());
    }

    @Test
    void toDto_missingFields_shouldNotCrashAndUseDefaults() {
        JsonObject o = new JsonObject();

        SellerItemDto dto = SellerItemDtoMapper.toDto(o);
        assertNotNull(dto);
        assertEquals(0, dto.getItemId());
        assertEquals("", dto.getName());
        assertEquals(0.0, dto.getStartPrice().doubleValue());
        assertEquals(0.0, dto.getCurrentPrice().doubleValue());
        assertEquals("", dto.getStatus());
    }
}
