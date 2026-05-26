package com.auction.server.feature.auction.service;

import com.auction.server.feature.auction.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AuctionScheduler.class);

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
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auction-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    /** Gọi 1 lần khi server khởi động */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::tick, 0, 10, TimeUnit.SECONDS);
        logger.info("AuctionScheduler đã khởi động, kiểm tra mỗi 10 giây");
    }

    public void stop() {
        scheduler.shutdownNow();
        logger.info("AuctionScheduler đã dừng");
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
                logger.info("Bắt đầu phiên auction_id={}", id);
            } catch (Exception e) {
                logger.error("Lỗi khi bắt đầu auction_id={}", id, e);
            }
        }
    }

    /** RUNNING → FINISHED/CANCELED: phiên đã hết giờ */
    private void finishExpiredAuctions() {
        List<Integer> ids = auctionRepository.findAuctionIdsToFinish();
        for (int id : ids) {
            try {
                auctionService.finishAuction(id);
                logger.info("Kết thúc phiên auction_id={}", id);
            } catch (Exception e) {
                logger.error("Lỗi khi kết thúc auction_id={}", id, e);
            }
        }
    }
}