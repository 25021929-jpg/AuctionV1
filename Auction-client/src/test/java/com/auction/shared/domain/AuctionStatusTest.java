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
    void fromString_caseInsensitive_parsesCurrentStatuses() {
        assertEquals(AuctionStatus.SCHEDULED, AuctionStatus.fromString("scheduled"));
        assertEquals(AuctionStatus.ACTIVE, AuctionStatus.fromString("ACTIVE"));
        assertEquals(AuctionStatus.ENDED, AuctionStatus.fromString("Ended"));
        assertEquals(AuctionStatus.CANCELLED, AuctionStatus.fromString("canceled"));
    }

    @Test
    void fromString_legacyStatuses_mapToCurrentStatuses() {
        assertEquals(AuctionStatus.SCHEDULED, AuctionStatus.fromString("OPEN"));
        assertEquals(AuctionStatus.ACTIVE, AuctionStatus.fromString("RUNNING"));
        assertEquals(AuctionStatus.ENDED, AuctionStatus.fromString("FINISHED"));
        assertEquals(AuctionStatus.ENDED, AuctionStatus.fromString("PAID"));
        assertEquals(AuctionStatus.CANCELLED, AuctionStatus.fromString("CANCELLED"));
    }

    @Test
    void isBiddable_onlyActive() {
        assertFalse(AuctionStatus.SCHEDULED.isBiddable());
        assertTrue(AuctionStatus.ACTIVE.isBiddable());
        assertFalse(AuctionStatus.ENDED.isBiddable());
        assertFalse(AuctionStatus.CANCELLED.isBiddable());
    }

    @Test
    void isFinishedLike_onlyEndedOrCanceled() {
        assertFalse(AuctionStatus.SCHEDULED.isFinishedLike());
        assertFalse(AuctionStatus.ACTIVE.isFinishedLike());
        assertTrue(AuctionStatus.ENDED.isFinishedLike());
        assertTrue(AuctionStatus.CANCELLED.isFinishedLike());
    }
}
