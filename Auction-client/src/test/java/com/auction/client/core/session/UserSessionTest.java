package com.auction.client.core.session;

import com.auction.shared.domain.UserRole;
import com.auction.shared.dto.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @AfterEach
    void tearDown() {
        UserSession.getInstance().clear();
    }

    @Test
    void emptySessionRepresentsGuest() {
        UserSession session = UserSession.getInstance();
        session.clear();

        assertFalse(session.isLoggedIn());
        assertNull(session.getUserId());
        assertNull(session.getRole());
        assertEquals("Khách", session.displayName());
    }

    @Test
    void sessionExposesCurrentUserRoleAndDisplayName() {
        UserSession session = UserSession.getInstance();
        session.setCurrentUser(new UserInfo(
                3L, "Seller A", "sellera", "seller@example.com", "0900000000", "2000-01-01", "SELLER"
        ));
        session.setToken("token-123");

        assertTrue(session.isLoggedIn());
        assertEquals(3L, session.getUserId());
        assertEquals("sellera", session.getUsername());
        assertEquals(UserRole.SELLER, session.getRole());
        assertTrue(session.hasRole(UserRole.SELLER));
        assertFalse(session.hasRole(UserRole.BIDDER));
        assertEquals("Seller A", session.displayName());
        assertEquals("token-123", session.getToken());
    }

    @Test
    void startUsesAuthResponseUser() {
        UserSession session = UserSession.getInstance();
        session.start(new com.auction.shared.dto.AuthResponse(new UserInfo(
                10L, "Bidder B", "bidderb", "b@example.com", null, null, "BIDDER"
        )));

        assertTrue(session.isLoggedIn());
        assertEquals(10L, session.getUserId());
        assertEquals(UserRole.BIDDER, session.getRole());
    }

    @Test
    void clearRemovesUserAndToken() {
        UserSession session = UserSession.getInstance();
        session.setCurrentUser(new UserInfo(1L, null, "admin", null, null, null, "ADMIN"));
        session.setToken("token-123");

        session.clear();

        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
        assertNull(session.getToken());
    }
}
