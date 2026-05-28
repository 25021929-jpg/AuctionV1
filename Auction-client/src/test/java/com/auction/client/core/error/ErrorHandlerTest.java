package com.auction.client.core.error;

import com.auction.shared.protocol.ActionConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorHandlerTest {

    @Test
    void connectionExceptionShouldReturnFriendlyMessage() {
        String message = ErrorHandler.getUserMessage(new ConnectionException("Không kết nối được server."));
        assertEquals("Không kết nối được server.", message);
    }

    @Test
    void timeoutExceptionShouldReturnFriendlyMessage() {
        String message = ErrorHandler.getUserMessage(new RequestTimeoutException("Server phản hồi quá lâu."));
        assertEquals("Server phản hồi quá lâu.", message);
    }

    @Test
    void serverBusinessExceptionShouldKeepServerMessage() {
        String message = ErrorHandler.getUserMessage(
                new ServerBusinessException(ActionConstants.BID_PLACE_BID, "Giá đặt phải lớn hơn giá hiện tại."));
        assertEquals("Giá đặt phải lớn hơn giá hiện tại.", message);
    }

    @Test
    void runtimeWrapperShouldBeUnwrapped() {
        String message = ErrorHandler.getUserMessage(
                new RuntimeException(new UnauthorizedException("Bạn cần đăng nhập để tiếp tục.")));
        assertEquals("Bạn cần đăng nhập để tiếp tục.", message);
    }

    @Test
    void unknownExceptionShouldHaveFallback() {
        String message = ErrorHandler.getUserMessage(new RuntimeException());
        assertTrue(message.contains("Đã xảy ra lỗi"));
    }
}
