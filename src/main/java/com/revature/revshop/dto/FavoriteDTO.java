package com.revature.revshop.dto;

public class FavoriteDTO {

    private Long productId;
    private String productName;

    public FavoriteDTO() {}

    public FavoriteDTO(Long productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
