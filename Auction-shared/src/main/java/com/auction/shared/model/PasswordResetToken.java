package com.auction.shared.model;

import java.time.LocalDateTime;

public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiredAt;
    private boolean used;

    public PasswordResetToken(
            Long id,
            Long userId,
            String token,
            LocalDateTime expiredAt,
            boolean used
    ) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiredAt = expiredAt;
        this.used = used;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public boolean isUsed() { return used; }
}