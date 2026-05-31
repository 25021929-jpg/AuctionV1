package com.auction.shared.dto.category;

/**
 * DTO danh mục sản phẩm gửi từ Server xuống Client.
 *
 * <p>Client không tự hard-code categoryId/categoryName. Danh mục hiển thị
 * trên giao diện Seller phải được lấy từ bảng categories của database thông
 * qua action CATEGORY_GET_LIST.</p>
 */
public class CategoryDto {

    private long categoryId;
    private String categoryName;
    private String slug;
    private Long parentId;

    public CategoryDto() {
    }

    public CategoryDto(long categoryId, String categoryName, String slug, Long parentId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.slug = slug;
        this.parentId = parentId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
