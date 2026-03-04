package com.revature.revshop.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private Long cartItemId;
    private Long productId;
    private Integer quantity;
    private String productName;
    private BigDecimal price;
    private String imageUrl;

    public CartItemDTO() {
    }

    public CartItemDTO(Long cartItemId, Long productId, Integer quantity,
            String productName, BigDecimal price, String imageUrl) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}