package com.auction.server.feature.auth.util;

public class HashGen {
    public static void main(String[] args) {
        System.out.println(PasswordUtil.hashPassword("123456789"));
    }
}