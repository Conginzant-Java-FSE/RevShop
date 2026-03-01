package com.revature.revshop.repository;

import com.revature.revshop.model.Orders;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByUser_UserId(Long userId);

    Optional<Orders> findByOrderNumber(String orderNumber);

    List<Orders> findByShippingAddress_AddressId(Long addressId);

    List<Orders> findByBillingAddress_AddressId(Long addressId);

    List<Orders> findByStatus(Orders.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT o FROM Orders o JOIN o.orderItems oi JOIN oi.product p WHERE p.seller.user.userId = :sellerId")
    List<Orders> findOrdersBySellerId(@org.springframework.data.repository.query.Param("sellerId") Long sellerId);
}
