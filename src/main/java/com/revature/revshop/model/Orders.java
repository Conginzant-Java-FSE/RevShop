package com.revature.revshop.model;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @ManyToOne
    @Column(name = "shipping_address_id")
    private Address shippingAddressId;

    @ManyToOne
    @Column(name = "billing_address_id")
    private Address billingAddressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "order_date")
    private LocalDateTime orderDate;


    public enum OrderStatus{
        PENDING,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    public Orders() {
    }


    public Orders(Long orderId, User user, String orderNumber, Address shippingAddressId, Address billingAddressId, OrderStatus status, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.user = user;
        this.orderNumber = orderNumber;
        this.shippingAddressId = shippingAddressId;
        this.billingAddressId = billingAddressId;
        this.status = status;
        this.orderDate = orderDate;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Address getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Address shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public Address getBillingAddressId() {
        return billingAddressId;
    }

    public void setBillingAddressId(Address billingAddressId) {
        this.billingAddressId = billingAddressId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}


