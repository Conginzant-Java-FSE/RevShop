package com.revature.revshop.dto;

public class CartItemDTO {
    private Integer productId;
    private Integer quantity;
    private String productName;
    private java.math.BigDecimal price;

    public CartItemDTO() {
    }

    public CartItemDTO(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItemDTO(Integer productId, Integer quantity, String productName, java.math.BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
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

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
}
