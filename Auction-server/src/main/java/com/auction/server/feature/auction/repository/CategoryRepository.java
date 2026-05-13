package com.auction.server.feature.auction.repository;

import com.auction.server.exception.DataAccessException;
import com.auction.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CategoryRepository {

    public boolean existsById(int categoryId) {
        String sql = """
                SELECT COUNT(*)
                FROM categories
                WHERE category_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

            return false;

        } catch (Exception e) {
            throw new DataAccessException("Error while checking category", e);
        }
    }
}