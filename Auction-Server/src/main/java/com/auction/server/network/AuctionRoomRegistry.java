package com.auction.server.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AuctionRoomRegistry {

    private static final ConcurrentHashMap<Long, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();

    private AuctionRoomRegistry() {
    }

    public static void join(Long auctionId, ClientHandler client) {
        if (auctionId == null || client == null) {
            return;
        }
        rooms.computeIfAbsent(auctionId, id -> ConcurrentHashMap.newKeySet()).add(client);
    }

    public static void leave(Long auctionId, ClientHandler client) {
        if (auctionId == null || client == null) {
            return;
        }

        Set<ClientHandler> room = rooms.get(auctionId);
        if (room == null) {
            return;
        }

        room.remove(client);
        if (room.isEmpty()) {
            rooms.remove(auctionId, room);
        }
    }

    public static void leaveAll(ClientHandler client) {
        if (client == null) {
            return;
        }

        for (Long auctionId : rooms.keySet()) {
            leave(auctionId, client);
        }
    }

    public static Set<ClientHandler> getViewers(Long auctionId) {
        Set<ClientHandler> room = rooms.get(auctionId);
        return room != null ? Set.copyOf(room) : Set.of();
    }
}
