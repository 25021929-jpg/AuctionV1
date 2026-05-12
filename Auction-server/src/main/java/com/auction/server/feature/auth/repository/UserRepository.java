package com.auction.server.feature.auth.repository;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.exception.DataAccessException;
import com.auction.shared.model.User;

import java.sql.*;

public class UserRepository {

    // =====================================================
    // Lưu user mới vào database
    // =====================================================
    public User save(User user) {

        String sql = """
                INSERT INTO users(
                    full_name,
                    username,
                    email,
                    phone,
                    date_of_birth,
                    password_hash,
                    role
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getDateOfBirth());
            ps.setString(6, user.getPasswordHash());
            ps.setString(7, user.getRole());

            ps.executeUpdate();

            // Lấy ID vừa được database tạo
            ResultSet keys = ps.getGeneratedKeys();

            if (keys.next()) {

                long generatedId = keys.getLong(1);

                return new User(
                        generatedId,
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getDateOfBirth(),
                        user.getPasswordHash(),
                        user.getRole()
                );
            }

            throw new DataAccessException(
                    "Cannot retrieve generated user ID",
                    null
            );

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error saving user",
                    e
            );
        }
    }

    // =====================================================
    // Kiểm tra username đã tồn tại chưa
    // =====================================================
    public boolean existsByUsername(String username) {

        String sql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error checking username",
                    e
            );
        }
    }

    // =====================================================
    // Kiểm tra email đã tồn tại chưa
    // =====================================================
    public boolean existsByEmail(String email) {

        String sql = """
                SELECT id
                FROM users
                WHERE email = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error checking email",
                    e
            );
        }
    }
    // =====================================================
// Tìm user bằng email
// =====================================================
    public User findByEmail(String email) {

        String sql = """
            SELECT *
            FROM users
            WHERE email = ?
            """;

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error finding user by email",
                    e
            );
        }
    }

    // =====================================================
    // Login bằng username HOẶC email
    // =====================================================
    public User findByLoginId(String loginId) {

        String sql = """
                SELECT *
                FROM users
                WHERE username = ?
                   OR email = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, loginId);
            ps.setString(2, loginId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error finding user",
                    e
            );
        }
    }

    // =====================================================
    // Đổi password
    // =====================================================
    public void updatePassword(
            Long userId,
            String newPasswordHash
    ) {

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE id = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);

            ps.executeUpdate();

        } catch (SQLException e) {

            throw new DataAccessException(
                    "Error updating password",
                    e
            );
        }
    }

    // =====================================================
    // Convert ResultSet -> User
    // =====================================================
    private User mapRow(ResultSet rs)
            throws SQLException {

        return new User(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("date_of_birth"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
    }
}