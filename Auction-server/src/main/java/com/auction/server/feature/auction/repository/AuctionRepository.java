package com.auction.server.feature.auction.repository;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.AuctionStatus;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy cập database cho feature auction.
 * Dùng JDBC thuần — không Spring, không JPA.
 *
 * SQL schema cần có:
 *   auctions(auction_id, item_id, seller_id, starting_price,
 *            current_price, current_winner_id, start_time, end_time, status)
 *   items(item_id, name, description, category, seller_id)
 *   users(user_id, username)
 *   bids(bid_id, auction_id, bidder_id, amount, bid_time)
 */
public class AuctionRepository {

    // ===================================================================
    // LẤY DANH SÁCH
    // ===================================================================

    public List<AuctionResponse> findAll() {
        String sql = """
                SELECT a.auction_id, a.item_id, i.name AS item_name,
                       a.starting_price, a.current_price,
                       a.start_time, a.end_time, a.status
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
                ORDER BY a.auction_id DESC
                """;

        List<AuctionResponse> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapToAuctionResponse(rs));
            }

        } catch (SQLException e) {
            throw new AuctionException("Lỗi khi lấy danh sách phiên đấu giá", e);
        }
        return list;
    }

    // ===================================================================
    // LẤY CHI TIẾT
    // ===================================================================

    public AuctionDetailResponse findDetailById(int auctionId) {
        String sql = """
                SELECT a.auction_id, a.item_id, i.name AS item_name,
                       i.description AS item_description, i.category AS item_category,
                       a.seller_id, u_seller.username AS seller_name,
                       a.starting_price, a.current_price,
                       a.current_winner_id, u_winner.username AS winner_name,
                       a.start_time, a.end_time, a.status
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
                JOIN users u_seller ON a.seller_id = u_seller.user_id
                LEFT JOIN users u_winner ON a.current_winner_id = u_winner.user_id
                WHERE a.auction_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AuctionException("Không tìm thấy phiên đấu giá id=" + auctionId);
                }
                // [FIX] Bỏ tính remainingSeconds ở đây — đã chuyển sang Service
                AuctionDetailResponse detail = mapToAuctionDetailResponse(rs);
                detail.setBidHistory(findBidHistory(conn, auctionId));
                return detail;
            }

        } catch (AuctionException e) {
            throw e;
        } catch (SQLException e) {
            throw new AuctionException("Lỗi khi lấy chi tiết phiên đấu giá", e);
        }
    }

    /** Lấy lịch sử bid của 1 phiên (dùng lại connection để tránh mở thêm) */
    private List<AuctionDetailResponse.BidHistoryItem> findBidHistory(
            Connection conn, int auctionId) throws SQLException {

        String sql = """
                SELECT b.bid_id, b.bidder_id, u.username AS bidder_name,
                       b.amount, b.bid_time
                FROM bids b
                JOIN users u ON b.bidder_id = u.user_id
                WHERE b.auction_id = ?
                ORDER BY b.bid_time ASC
                """;

        List<AuctionDetailResponse.BidHistoryItem> history = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new AuctionDetailResponse.BidHistoryItem(
                            rs.getInt("bid_id"),
                            rs.getInt("bidder_id"),
                            rs.getString("bidder_name"),
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("bid_time").toLocalDateTime()
                    ));
                }
            }
        }
        return history;
    }

    // ===================================================================
    // TẠO PHIÊN ĐẤU GIÁ
    // ===================================================================

    public AuctionResponse create(int itemId, int sellerId, BigDecimal startingPrice,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
                INSERT INTO auctions
                    (item_id, seller_id, starting_price, current_price,
                     start_time, end_time, status)
                VALUES (?, ?, ?, ?, ?, ?, 'OPEN')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, itemId);
            ps.setInt(2, sellerId);
            ps.setBigDecimal(3, startingPrice);
            ps.setBigDecimal(4, startingPrice); // current_price ban đầu = starting_price
            ps.setTimestamp(5, Timestamp.valueOf(startTime));
            ps.setTimestamp(6, Timestamp.valueOf(endTime));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
            throw new AuctionException("Tạo phiên đấu giá thất bại");

        } catch (AuctionException e) {
            throw e;
        } catch (SQLException e) {
            throw new AuctionException("Lỗi khi tạo phiên đấu giá", e);
        }
    }

    // ===================================================================
    // CẬP NHẬT TRẠNG THÁI
    // ===================================================================

    /**
     * Cập nhật giá + người dẫn đầu sau mỗi bid hợp lệ.
     * Dùng synchronized ở BiddingService để tránh race condition.
     */
    public void updateCurrentPrice(Connection conn, int auctionId,
                                   BigDecimal newPrice, int winnerId) throws SQLException {
        String sql = """
                UPDATE auctions
                SET current_price = ?, current_winner_id = ?
                WHERE auction_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newPrice);
            ps.setInt(2, winnerId);
            ps.setInt(3, auctionId);
            ps.executeUpdate();
        }
    }

    /**
     * [FIX] UPDATE có điều kiện: chỉ cập nhật khi status hiện tại = expectedStatus.
     * Trả về số dòng bị ảnh hưởng — Service dùng để biết update có thành công không.
     *
     * Thay thế cho pattern: SELECT để check status → UPDATE riêng (2 query, có race condition).
     * Ví dụ: UPDATE auctions SET status='RUNNING' WHERE auction_id=? AND status='OPEN'
     */
    public int updateStatusConditional(int auctionId,
                                       AuctionStatus expectedStatus,
                                       AuctionStatus newStatus) {
        String sql = "UPDATE auctions SET status = ? WHERE auction_id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, auctionId);
            ps.setString(3, expectedStatus.name());
            return ps.executeUpdate(); // 1 nếu thành công, 0 nếu status không khớp
        } catch (SQLException e) {
            throw new AuctionException("Lỗi cập nhật trạng thái auction", e);
        }
    }

    /**
     * [FIX] UPDATE trực tiếp không điều kiện — dùng cho cancelAuction
     * khi Service đã tự kiểm tra điều kiện nghiệp vụ trước.
     */
    public void updateStatusDirect(int auctionId, AuctionStatus newStatus) {
        String sql = "UPDATE auctions SET status = ? WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AuctionException("Lỗi cập nhật trạng thái auction", e);
        }
    }

    /** RUNNING → FINISHED hoặc CANCELED */
    public void updateStatusToFinished(int auctionId, boolean hasBid) {
        updateStatusDirect(auctionId, hasBid ? AuctionStatus.FINISHED : AuctionStatus.CANCELED);
    }

    /** Gia hạn phiên (Anti-sniping) */
    public void extendEndTime(int auctionId, LocalDateTime newEndTime) {
        String sql = "UPDATE auctions SET end_time = ? WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newEndTime));
            ps.setInt(2, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AuctionException("Lỗi khi gia hạn phiên đấu giá", e);
        }
    }

    // ===================================================================
    // SCHEDULER — tìm phiên cần chuyển trạng thái
    // ===================================================================

    /** Tìm phiên OPEN đã đến giờ bắt đầu */
    public List<Integer> findAuctionIdsToStart() {
        return findIdsByStatusAndTime(
                "SELECT auction_id FROM auctions WHERE status='OPEN' AND start_time <= NOW()");
    }

    /** Tìm phiên RUNNING đã hết giờ */
    public List<Integer> findAuctionIdsToFinish() {
        return findIdsByStatusAndTime(
                "SELECT auction_id FROM auctions WHERE status='RUNNING' AND end_time <= NOW()");
    }

    private List<Integer> findIdsByStatusAndTime(String sql) {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("auction_id"));
        } catch (SQLException e) {
            throw new AuctionException("Lỗi scheduler query", e);
        }
        return ids;
    }

    // ===================================================================
    // HELPER
    // ===================================================================

    public AuctionResponse findById(int auctionId) {
        String sql = """
                SELECT a.auction_id, a.item_id, i.name AS item_name,
                       a.starting_price, a.current_price,
                       a.start_time, a.end_time, a.status
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
                WHERE a.auction_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AuctionException("Không tìm thấy auction id=" + auctionId);
                return mapToAuctionResponse(rs);
            }
        } catch (AuctionException e) {
            throw e;
        } catch (SQLException e) {
            throw new AuctionException("Lỗi truy vấn auction", e);
        }
    }

    private AuctionResponse mapToAuctionResponse(ResultSet rs) throws SQLException {
        AuctionResponse r = new AuctionResponse();
        r.setAuctionId(rs.getInt("auction_id"));
        r.setItemId(rs.getInt("item_id"));
        r.setItemName(rs.getString("item_name"));
        r.setStartingPrice(rs.getBigDecimal("starting_price"));
        r.setCurrentPrice(rs.getBigDecimal("current_price"));
        r.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        r.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        r.setStatus(rs.getString("status"));
        return r;
    }

    private AuctionDetailResponse mapToAuctionDetailResponse(ResultSet rs) throws SQLException {
        AuctionDetailResponse d = new AuctionDetailResponse();
        d.setAuctionId(rs.getInt("auction_id"));
        d.setItemId(rs.getInt("item_id"));
        d.setItemName(rs.getString("item_name"));
        d.setItemDescription(rs.getString("item_description"));
        d.setItemCategory(rs.getString("item_category"));
        d.setSellerId(rs.getInt("seller_id"));
        d.setSellerName(rs.getString("seller_name"));
        d.setStartingPrice(rs.getBigDecimal("starting_price"));
        d.setCurrentPrice(rs.getBigDecimal("current_price"));

        int winnerId = rs.getInt("current_winner_id");
        if (!rs.wasNull()) {
            d.setCurrentWinnerId(winnerId);
            d.setCurrentWinnerName(rs.getString("winner_name"));
        }

        d.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        d.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        d.setStatus(rs.getString("status"));
        // [FIX] Không tính remainingSeconds ở đây nữa — đã chuyển sang Service
        return d;
    }
}
