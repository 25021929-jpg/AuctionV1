package com.auction.shared.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionStatusTest {

    @Test
    void fromString_nullOrBlank_returnsNull() {
        assertNull(AuctionStatus.fromString(null));
        assertNull(AuctionStatus.fromString(""));
        assertNull(AuctionStatus.fromString("   "));
    }

    @Test
    void fromString_caseInsensitive_parses() {
        assertEquals(AuctionStatus.OPEN, AuctionStatus.fromString("open"));
        assertEquals(AuctionStatus.RUNNING, AuctionStatus.fromString("RUNNING"));
        assertEquals(AuctionStatus.FINISHED, AuctionStatus.fromString("Finished"));
    }

    @Test
    void fromString_cancelled_variant_mapsToCanceled() {
        assertEquals(AuctionStatus.CANCELED, AuctionStatus.fromString("CANCELLED"));
        assertEquals(AuctionStatus.CANCELED, AuctionStatus.fromString("cancelled"));
    }

    @Test
    void isBiddable_onlyOpenOrRunning() {
        assertTrue(AuctionStatus.OPEN.isBiddable());
        assertTrue(AuctionStatus.RUNNING.isBiddable());
        assertFalse(AuctionStatus.FINISHED.isBiddable());
        assertFalse(AuctionStatus.PAID.isBiddable());
        assertFalse(AuctionStatus.CANCELED.isBiddable());
    }
}
