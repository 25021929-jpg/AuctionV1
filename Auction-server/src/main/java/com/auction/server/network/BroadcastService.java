package com.auction.server.network;

import com.auction.shared.protocol.WireMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BroadcastService {

    private static final ExecutorService pool = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "auction-broadcast-worker");
        thread.setDaemon(true);
        return thread;
    });

    private BroadcastService() {
    }

    public static void broadcastToRoom(Long auctionId, WireMessage event) {
        if (auctionId == null || event == null) {
            return;
        }

        for (ClientHandler client : AuctionRoomRegistry.getViewers(auctionId)) {
            pool.submit(() -> {
                boolean sent = client.send(event);
                if (!sent) {
                    AuctionRoomRegistry.leaveAll(client);
                }
            });
        }
    }
}
