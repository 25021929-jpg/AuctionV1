package com.auction.server.feature.auction.service;

import com.auction.server.feature.auction.repository.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler tự động kiểm tra và chuyển trạng thái auction mỗi 10 giây.
 *
 * Không dùng Spring @Scheduled — dùng Java ScheduledExecutorService thuần.
 *
 * Cách dùng: gọi AuctionScheduler.getInstance().start() trong MainServer.
 *
 * Design Pattern: Singleton — đảm bảo chỉ 1 scheduler chạy.
 */
public class AuctionScheduler {

    // ---------------------------------------------------------------
    // Singleton
    // ---------------------------------------------------------------
    private static AuctionScheduler instance;

    public static synchronized AuctionScheduler getInstance() {
        if (instance == null) {
            instance = new AuctionScheduler();
        }
        return instance;
    }

    // ---------------------------------------------------------------
    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;
    private final ScheduledExecutorService scheduler;

    private AuctionScheduler() {
        this.auctionRepository = new AuctionRepository();
        this.auctionService = new AuctionService();
        // 1 thread riêng cho scheduler — không ảnh hưởng luồng xử lý client
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auction-scheduler");
            t.setDaemon(true); // tự tắt khi JVM tắt
            return t;
        });
    }

    /** Gọi 1 lần khi server khởi động */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::tick, 0, 10, TimeUnit.SECONDS);
        System.out.println("[AuctionScheduler] Đã khởi động, kiểm tra mỗi 10 giây");
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    // ---------------------------------------------------------------
    // Logic kiểm tra mỗi tick
    // ---------------------------------------------------------------

    private void tick() {
        startPendingAuctions();
        finishExpiredAuctions();
    }

    /** OPEN → RUNNING: phiên đã đến giờ bắt đầu */
    private void startPendingAuctions() {
        List<Integer> ids = auctionRepository.findAuctionIdsToStart();
        for (int id : ids) {
            try {
                auctionService.startAuction(id);
                System.out.println("[Scheduler] Bắt đầu phiên auction_id=" + id);
            } catch (Exception e) {
                System.err.println("[Scheduler] Lỗi khi bắt đầu auction_id=" + id + ": " + e.getMessage());
            }
        }
    }

    /** RUNNING → FINISHED/CANCELED: phiên đã hết giờ */
    private void finishExpiredAuctions() {
        List<Integer> ids = auctionRepository.findAuctionIdsToFinish();
        for (int id : ids) {
            try {
                auctionService.finishAuction(id);
                System.out.println("[Scheduler] Kết thúc phiên auction_id=" + id);
            } catch (Exception e) {
                System.err.println("[Scheduler] Lỗi khi kết thúc auction_id=" + id + ": " + e.getMessage());
            }
        }
    }
}
