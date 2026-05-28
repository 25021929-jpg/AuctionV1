package com.auction.client.core.ui;

/**
 * Controller có tài nguyên cần dọn khi rời màn hình.
 *
 * <p>Ví dụ: unsubscribe EventBus, unsubscribe socket realtime, đóng timer.</p>
 */
public interface DisposableController {
    void dispose();
}
