package com.auction.server.network;

import com.auction.shared.protocol.WireMessage;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BroadcastService {

    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private static final ExecutorService pool = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "auction-broadcast-worker");
        thread.setDaemon(true);
        return thread;
    });

    private BroadcastService() {
    }

    public static void register(ClientHandler client) {
        if (client != null) {
            clients.add(client);
        }
    }

    public static void unregister(ClientHandler client) {
        if (client != null) {
            clients.remove(client);
        }
    }

    public static void broadcastToAll(WireMessage event) {
        if (event == null) {
            return;
        }

        for (ClientHandler client : Set.copyOf(clients)) {
            pool.submit(() -> {
                boolean sent = client.send(event);
                if (!sent) {
                    unregister(client);
                    AuctionRoomRegistry.leaveAll(client);
                }
            });
        }
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
