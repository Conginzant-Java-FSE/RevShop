package com.revature.revshop.service;


import com.revature.revshop.model.OrderItems;
import com.revature.revshop.repository.OrderItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderItemsService {

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    public OrderItems createOrderItem(OrderItems orderItem) {
        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        return orderItemsRepository.save(orderItem);
    }

    public Optional<OrderItems> getOrderItemById(Integer orderItemId) {
        return orderItemsRepository.findById(orderItemId);
    }

    public List<OrderItems> getAllOrderItems() {
        return orderItemsRepository.findAll();
    }

    public List<OrderItems> getOrderItemsByOrderId(Long orderId) {
        return orderItemsRepository.findByOrder_OrderId(orderId);
    }

    public List<OrderItems> getOrderItemsByProductId(Long productId) {
        return orderItemsRepository.findByProduct_ProductId(productId);
    }

    public OrderItems updateOrderItem(OrderItems orderItem) {
        if (!orderItemsRepository.existsById(orderItem.getOrderItemId())) {
            throw new RuntimeException("OrderItem not found with id: " + orderItem.getOrderItemId());
        }

        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        return orderItemsRepository.save(orderItem);
    }

    public void deleteOrderItem(Integer orderItemId) {
        if (!orderItemsRepository.existsById(orderItemId)) {
            throw new RuntimeException("OrderItem not found with id: " + orderItemId);
        }
        orderItemsRepository.deleteById(orderItemId);
    }


}
