package com.auction.server.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "bids")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"bidId"})
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionSession auctionSession;
    // Tên field: auctionSession (không phải auction)
    // Tên cột FK: auction_id (trong bảng bids)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "bid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal bidAmount;

    @Column(name = "bid_time", nullable = false)
    @Builder.Default
    private LocalDateTime bidTime = LocalDateTime.now();

    @Column(name = "is_winning", nullable = false)
    @Builder.Default
    private Boolean isWinning = false;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    // VARCHAR(45): đủ cho cả IPv4 (15 ký tự) và IPv6 (39 ký tự)
}
