package com.auction.client.core.ui;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Helper chạy tác vụ nền (network/IO) và trả kết quả về JavaFX UI thread.
 *
 * <p>Mục tiêu: giảm việc block UI thread khi gọi Socket/IO, đồng thời hạn chế
 * việc viết lặp code Task/Thread trong từng Controller.</p>
 */
public final class FxAsync {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new DaemonThreadFactory());

    private FxAsync() {
    }

    public static <T> void run(
            Supplier<T> background,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            Runnable onFinally
    ) {
        Objects.requireNonNull(background, "background");
        EXECUTOR.execute(() -> {
            try {
                T result = background.get();
                if (onSuccess != null) {
                    Platform.runLater(() -> onSuccess.accept(result));
                }
            } catch (Throwable t) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(t));
                }
            } finally {
                if (onFinally != null) {
                    Platform.runLater(onFinally);
                }
            }
        });
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("fx-async-" + t.getId());
            t.setDaemon(true);
            return t;
        }
    }
}
