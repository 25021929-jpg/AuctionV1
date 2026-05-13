package com.auction.server.feature.auth.repository;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.exception.DataAccessException;
import com.auction.shared.model.PasswordResetToken;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetRepository {

    // Lưu token reset password
    public void saveToken(
            Long userId,
            String token,
            LocalDateTime expiredAt
    ) {

        String sql = """
                INSERT INTO password_reset_tokens(
                    user_id,
                    token,
                    expired_at
                )
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setLong(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(expiredAt)
            );

            ps.executeUpdate();

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error saving reset token",
                    e
            );
        }
    }

    // Tìm token
    public PasswordResetToken findByToken(String token) {

        String sql = """
                SELECT *
                FROM password_reset_tokens
                WHERE token = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, token);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new PasswordResetToken(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("token"),
                        rs.getTimestamp("expired_at")
                                .toLocalDateTime(),
                        rs.getBoolean("used")
                );
            }

            return null;

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error finding reset token",
                    e
            );
        }
    }

    // Đánh dấu token đã dùng
    public void markUsed(Long tokenId) {

        String sql = """
                UPDATE password_reset_tokens
                SET used = true
                WHERE id = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setLong(1, tokenId);

            ps.executeUpdate();

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error updating token",
                    e
            );
        }
    }
}