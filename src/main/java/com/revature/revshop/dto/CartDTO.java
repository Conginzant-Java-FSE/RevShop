package com.revature.revshop.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {
    private Integer cartId;
    private BigDecimal totalPrice;
    private List<CartItemDTO> items;

    public CartDTO() {
    }

    public CartDTO(Integer cartId, BigDecimal totalPrice, List<CartItemDTO> items) {
        this.cartId = cartId;
        this.totalPrice = totalPrice;
        this.items = items;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
}
