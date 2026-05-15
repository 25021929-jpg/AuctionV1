package com.auction.server;

import com.auction.server.network.ServerSocketManager;

public class MainServer {
    public static void main(String[] args) {
        new ServerSocketManager(8080).start();
    }
}