package com.auction.server.feature.auction.repository;

import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionSessionRepository {

    public int save(
            int itemId,
            BigDecimal startingPrice,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        String sql = """
                INSERT INTO auction_sessions (
                    item_id,
                    starting_price,
                    current_price,
                    start_time,
                    end_time,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            statement.setInt(1, itemId);
            statement.setBigDecimal(2, startingPrice);
            statement.setBigDecimal(3, startingPrice);
            statement.setTimestamp(4, Timestamp.valueOf(startTime));
            statement.setTimestamp(5, Timestamp.valueOf(endTime));
            statement.setString(6, "UPCOMING");

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("Create auction session failed",null);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new DataAccessException("Create auction session failed, no ID returned",null);

        } catch (Exception e) {
            throw new DataAccessException("Error while saving auction session", e);
        }
    }

    public List<AuctionResponse> findAll() {
        String sql = """
                SELECT
                    s.auction_id,
                    i.item_id,
                    i.item_name,
                    s.starting_price,
                    s.current_price,
                    s.start_time,
                    s.end_time,
                    s.status
                FROM auction_sessions s
                JOIN auction_items i ON s.item_id = i.item_id
                ORDER BY s.start_time DESC
                """;

        List<AuctionResponse> auctions = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                AuctionResponse response = new AuctionResponse(
                        resultSet.getInt("auction_id"),
                        resultSet.getInt("item_id"),
                        resultSet.getString("item_name"),
                        resultSet.getBigDecimal("starting_price"),
                        resultSet.getBigDecimal("current_price"),
                        resultSet.getTimestamp("start_time").toLocalDateTime(),
                        resultSet.getTimestamp("end_time").toLocalDateTime(),
                        resultSet.getString("status")
                );

                auctions.add(response);
            }

            return auctions;

        } catch (Exception e) {
            throw new DataAccessException("Error while finding all auction sessions", e);
        }
    }

    public AuctionDetailResponse findDetailById(int auctionId) {
        String sql = """
                SELECT
                    s.auction_id,
                    i.item_id,
                    i.item_name,
                    i.description,
                    c.category_name,
                    u.full_name AS seller_name,
                    s.starting_price,
                    s.current_price,
                    s.start_time,
                    s.end_time,
                    s.status
                FROM auction_sessions s
                JOIN auction_items i ON s.item_id = i.item_id
                JOIN categories c ON i.category_id = c.category_id
                JOIN users u ON i.seller_id = u.user_id
                WHERE s.auction_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, auctionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new AuctionDetailResponse(
                            resultSet.getInt("auction_id"),
                            resultSet.getInt("item_id"),
                            resultSet.getString("item_name"),
                            resultSet.getString("description"),
                            resultSet.getString("category_name"),
                            resultSet.getString("seller_name"),
                            resultSet.getBigDecimal("starting_price"),
                            resultSet.getBigDecimal("current_price"),
                            resultSet.getTimestamp("start_time").toLocalDateTime(),
                            resultSet.getTimestamp("end_time").toLocalDateTime(),
                            resultSet.getString("status")
                    );
                }

                return null;
            }

        } catch (Exception e) {
            throw new DataAccessException("Error while finding auction detail", e);
        }
    }

    public boolean existsById(int auctionId) {
        String sql = """
                SELECT COUNT(*)
                FROM auction_sessions
                WHERE auction_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, auctionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

            return false;

        } catch (Exception e) {
            throw new DataAccessException("Error while checking auction session", e);
        }
    }
}