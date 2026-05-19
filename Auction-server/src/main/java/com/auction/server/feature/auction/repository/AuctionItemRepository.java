package com.auction.server.feature.auction.repository;

import com.auction.server.exception.DataAccessException;
import com.auction.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuctionItemRepository {

    public int save(
            int sellerId,
            int categoryId,
            String itemName,
            String description
    ) { // chèn vật phẩm vào hàng đấu giá
        String sql = """
                INSERT INTO auction_items (
                    seller_id,
                    category_id,
                    item_name,
                    description
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            statement.setInt(1, sellerId);
            statement.setInt(2, categoryId);
            statement.setString(3, itemName);
            statement.setString(4, description);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("Create auction item failed",null);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            throw new DataAccessException("Create auction item failed, no ID returned",null);

        } catch (Exception e) {
            throw new DataAccessException("Error while saving auction item", e);
        }
    }

    public boolean existsById(int itemId) {
        String sql = """
                SELECT COUNT(*)
                FROM auction_items
                WHERE item_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

            return false;

        } catch (Exception e) {
            throw new DataAccessException("Error while checking auction item", e);
        }
    }
}