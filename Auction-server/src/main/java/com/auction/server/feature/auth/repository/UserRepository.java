package com.auction.server.feature.auth.repository;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.exception.DataAccessException;
import com.auction.shared.model.User;

import java.sql.*;

public class UserRepository {

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new DataAccessException("Error checking username existence", e);
        }
    }

    public User save(String username, String passwordHash, String fullName, String role) {
        String sql = """
                INSERT INTO users(username, password_hash, full_name, role)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role);

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();

            if (keys.next()) {
                long id = keys.getLong(1);
                return new User(id, username, passwordHash, fullName, role);
            }

            throw new DataAccessException("Cannot retrieve generated user ID", null);

        } catch (SQLException e) {
            throw new DataAccessException("Error saving user", e);
        }
    }

    public User findByUsername(String username) {
        String sql = """
                SELECT id, username, password_hash, full_name, role
                FROM users
                WHERE username = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            return mapRow(rs);

        } catch (SQLException e) {
            throw new DataAccessException("Error finding user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getString("role")
        );
    }
}