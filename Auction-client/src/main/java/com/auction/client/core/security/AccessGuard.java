package com.auction.client.core.security;

import com.auction.client.core.error.ForbiddenException;
import com.auction.client.core.error.UnauthorizedException;
import com.auction.client.core.session.UserSession;
import com.auction.shared.domain.UserRole;

/** Guard quyền truy cập màn hình/chức năng phía client. */
public final class AccessGuard {

    private AccessGuard() {
    }

    public static void requireLogin() throws UnauthorizedException {
        if (!UserSession.getInstance().isLoggedIn()) {
            throw new UnauthorizedException("Bạn cần đăng nhập để tiếp tục.");
        }
    }

    public static void requireRole(UserRole role) throws UnauthorizedException, ForbiddenException {
        requireLogin();
        if (!UserSession.getInstance().hasRole(role)) {
            throw new ForbiddenException("Bạn không có quyền truy cập chức năng này.");
        }
    }

    public static void requireAnyRole(UserRole... roles) throws UnauthorizedException, ForbiddenException {
        requireLogin();
        if (roles == null || roles.length == 0) {
            return;
        }
        for (UserRole role : roles) {
            if (UserSession.getInstance().hasRole(role)) {
                return;
            }
        }
        throw new ForbiddenException("Bạn không có quyền truy cập chức năng này.");
    }
}
