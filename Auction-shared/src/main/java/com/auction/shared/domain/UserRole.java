package com.auction.shared.domain;

/**
 * Các vai trò người dùng theo đề bài: Bidder / Seller / Admin.
 */
public enum UserRole {
    BIDDER,
    SELLER,
    ADMIN;

    /**
     * Parse role từ string (tolerant).
     * @param raw role có thể null/khác format
     * @return UserRole hoặc null nếu không parse được
     */
    public static UserRole fromString(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase();
        if (s.isEmpty()) return null;
        try {
            return UserRole.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
