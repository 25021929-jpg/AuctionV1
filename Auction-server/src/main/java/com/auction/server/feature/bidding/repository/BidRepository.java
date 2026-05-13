package com.auction.server.feature.bidding.repository;

import com.auction.server.exception.DataAccessException;
import com.auction.server.database.DatabaseConnection;

import java.sql.*;

public class BidRepository {

    public double findCurrentPriceByAuctionSessionId(int auctionSessionId) {
        String sql = """
                SELECT current_price
                FROM auction_sessions
                WHERE id = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, auctionSessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("current_price");
                }
            }

            throw new DataAccessException("Auction session not found",null);

        } catch (SQLException e) {
            throw new DataAccessException("Error while finding current price", e);
        }
    }

    public boolean existsAuctionSessionById(int auctionSessionId) {
        String sql = """
                SELECT id
                FROM auction_sessions
                WHERE id = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, auctionSessionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error while checking auction session", e);
        }
    }

    public int saveBid(int auctionSessionId, int bidderId, double bidAmount) {
        String sql = """
                INSERT INTO bids (auction_session_id, bidder_id, bid_amount)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, auctionSessionId);
            ps.setInt(2, bidderId);
            ps.setDouble(3, bidAmount);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException("Cannot get generated bid id",null);

        } catch (SQLException e) {
            throw new DataAccessException("Error while saving bid", e);
        }
    }

    public void updateCurrentPrice(int auctionSessionId, double newPrice) {
        String sql = """
                UPDATE auction_sessions
                SET current_price = ?
                WHERE id = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, auctionSessionId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error while updating current price", e);
        }
    }
}