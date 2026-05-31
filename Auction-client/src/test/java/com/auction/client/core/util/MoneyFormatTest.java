package com.auction.client.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyFormatTest {

    @Test
    void parseAcceptsPlainNumber() {
        assertEquals(new BigDecimal("1500000"), MoneyFormat.parse("1500000"));
    }

    @Test
    void parseAcceptsCommaDecimal() {
        assertEquals(new BigDecimal("1500.50"), MoneyFormat.parse("1500,50"));
    }



    @Test
    void parseAcceptsVietnameseThousandsSeparator() {
        assertEquals(new BigDecimal("1000000"), MoneyFormat.parse("1.000.000"));
    }

    @Test
    void parseRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> MoneyFormat.parse("  "));
    }
}
