package com.auction.server.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "item_images")
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private AuctionItem item;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public ItemImage() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuctionItem getItem() { return item; }
    public void setItem(AuctionItem item) { this.item = item; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean primary) { isPrimary = primary; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemImage itemImage)) return false;
        return Objects.equals(id, itemImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
