package com.revature.revshop.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orderId;

//    @ManyToOne
//    @JoinColumn(name = "product_id", nullable = false)
//    private Product productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;

//    public OrderItems(Integer orderItemId, Orders orderId, Product productId, Integer quantity, BigDecimal priceAtPurchase) {
//        this.orderItemId = orderItemId;
//        this.orderId = orderId;
//        this.productId = productId;
//        this.quantity = quantity;
//        this.priceAtPurchase = priceAtPurchase;
//    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Orders getOrderId() {
        return orderId;
    }

    public void setOrderId(Orders orderId) {
        this.orderId = orderId;
    }

//    public Product getProductId() {
//        return productId;
//    }
//
//    public void setProductId(Product productId) {
//        this.productId = productId;
//    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
