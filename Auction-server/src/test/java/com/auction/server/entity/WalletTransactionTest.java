package com.auction.server.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WalletTransactionTest {

    @Test
    void testGettersAndSetters() {
        WalletTransaction tx = new WalletTransaction();
        User mockUser = new User();

        tx.setTransactionId(999L);
        tx.setUser(mockUser);
        tx.setAuctionSession(null); // Giả lập không qua đấu giá
        tx.setType(WalletTransaction.TransactionType.DEPOSIT);
        tx.setAmount(new BigDecimal("100000"));
        tx.setBalanceAfter(new BigDecimal("100000"));
        tx.setDescription("Nạp tiền test");

        LocalDateTime now = LocalDateTime.now();
        tx.setCreatedAt(now);

        // Kiểm tra dữ liệu
        assertEquals(999L, tx.getTransactionId());
        assertEquals(mockUser, tx.getUser());
        assertNull(tx.getAuctionSession());
        assertEquals(WalletTransaction.TransactionType.DEPOSIT, tx.getType());
        assertEquals(new BigDecimal("100000"), tx.getAmount());
        assertEquals(new BigDecimal("100000"), tx.getBalanceAfter());
        assertEquals("Nạp tiền test", tx.getDescription());
        assertEquals(now, tx.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        WalletTransaction tx1 = new WalletTransaction();
        tx1.setTransactionId(55L);

        WalletTransaction tx2 = new WalletTransaction();
        tx2.setTransactionId(55L);

        WalletTransaction tx3 = new WalletTransaction();
        tx3.setTransactionId(66L);

        // Kiểm tra hàm equals
        assertEquals(tx1, tx1);
        assertEquals(tx1, tx2);
        assertNotEquals(tx1, tx3);
        assertNotEquals(tx1, null);

        // Kiểm tra hashCode
        assertEquals(tx1.hashCode(), tx2.hashCode());
        assertNotEquals(tx1.hashCode(), tx3.hashCode());
    }
}