package com.auction.server.repository.base;

import com.auction.server.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> {

    protected abstract T mapRow(ResultSet rs) throws SQLException;

    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    protected Optional<T> executeQueryOne(String sql, Object... params) throws SQLException {
        List<T> list = executeQuery(sql, params);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    protected long executeInsert(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        return -1L;
    }

    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    protected boolean exists(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    protected void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object val = params[i];
            int pos = i + 1;
            if (val == null)                    ps.setNull(pos, Types.NULL);
            else if (val instanceof String)     ps.setString(pos, (String) val);
            else if (val instanceof Integer)    ps.setInt(pos, (Integer) val);
            else if (val instanceof Long)       ps.setLong(pos, (Long) val);
            else if (val instanceof Double)     ps.setDouble(pos, (Double) val);
            else if (val instanceof Boolean)    ps.setBoolean(pos, (Boolean) val);
            else if (val instanceof LocalDateTime)
                ps.setTimestamp(pos, Timestamp.valueOf((LocalDateTime) val));
            else ps.setObject(pos, val);
        }
    }

    protected LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toLocalDateTime();
    }

    protected Long getNullableLong(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }
}