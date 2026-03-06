package com.revature.revshop.repository;

import com.revature.revshop.model.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Integer> {

    List<OrderItems> findByOrderOrderId(Long orderId);

    List<OrderItems> findByProductProductId(Long productId);
}
