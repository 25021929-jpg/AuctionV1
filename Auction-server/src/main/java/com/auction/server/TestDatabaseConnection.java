package com.auction.server;

import com.auction.server.database.DatabaseConnection;

import java.sql.Connection;

public class TestDatabaseConnection {

    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection();

            System.out.println("Connect database success!");

            connection.close();

        } catch (Exception e) {
            System.out.println("Connect database failed!");
            e.printStackTrace();
        }
    }
}