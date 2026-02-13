package com.revature.revshop.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDTO {

    private Long productId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal mrp;

    @NotNull
    @Positive
    private BigDecimal sellingPrice;

    @NotNull
    private Integer stockQuantity;

    @NotNull
    private Integer thresholdQuantity;

    private Boolean isActive = true;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    public ProductDTO() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getThresholdQuantity() { return thresholdQuantity; }
    public void setThresholdQuantity(Integer thresholdQuantity) { this.thresholdQuantity = thresholdQuantity; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { this.isActive = active; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
}
