package com.revature.revshop.repository;

import com.revature.revshop.model.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Integer> {

    List<OrderItems> findByOrder_OrderId(Long orderId);

    List<OrderItems> findByProduct_ProductId(Long productId);
}
