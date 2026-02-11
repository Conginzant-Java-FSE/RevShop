package com.revature.revshop.service;

import com.revature.revshop.model.OrderItems;
import com.revature.revshop.model.Orders;
import com.revature.revshop.repository.OrderItemsRepository;
import com.revature.revshop.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    public Orders createOrder(Orders order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus(Orders.OrderStatus.PENDING);
        }
        return ordersRepository.save(order);
    }

    public Optional<Orders> getOrderById(Long orderId) {
        return ordersRepository.findById(orderId);
    }

    public Optional<Orders> getOrderByOrderNumber(String orderNumber) {
        return ordersRepository.findByOrderNumber(orderNumber);
    }

    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    public List<Orders> getOrdersByStatus(Orders.OrderStatus status) {
        return ordersRepository.findByStatus(status);
    }


    public List<Orders> getOrdersByShippingAddressId(Long addressId) {
        return ordersRepository.findByShippingAddress_AddressId(addressId);
    }


    public List<Orders> getOrdersByBillingAddressId(Long addressId) {
        return ordersRepository.findByBillingAddress_AddressId(addressId);
    }

    public Orders updateOrder(Orders order) {
        if (!ordersRepository.existsById(order.getOrderId())) {
            throw new RuntimeException("Order not found with id: " + order.getOrderId());
        }
        return ordersRepository.save(order);
    }

    public Orders updateOrderStatus(Long orderId, Orders.OrderStatus status) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(status);
        return ordersRepository.save(order);
    }

    public void deleteOrder(Long orderId) {
        if (!ordersRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        ordersRepository.deleteById(orderId);
    }

    public void calculateOrderTotal(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        List<OrderItems> orderItems = orderItemsRepository.findByOrder_OrderId(orderId);

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        ordersRepository.save(order);
    }

}
