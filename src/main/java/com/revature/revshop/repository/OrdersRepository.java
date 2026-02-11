package com.revature.revshop.repository;

import com.revature.revshop.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
