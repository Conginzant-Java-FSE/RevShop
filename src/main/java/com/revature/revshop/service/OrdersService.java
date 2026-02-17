package com.revature.revshop.service;

import com.revature.revshop.dto.OrderItemRequestDTO;
import com.revature.revshop.dto.OrderItemResponseDTO;
import com.revature.revshop.dto.OrderRequestDTO;
import com.revature.revshop.dto.OrderResponseDTO;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    public OrdersService(OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
    }

    public OrderResponseDTO placeOrder(Long userId, OrderRequestDTO request) {

        // Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate Addresses
        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        Address billingAddress = addressRepository.findById(request.getBillingAddressId())
                .orElseThrow(() -> new RuntimeException("Billing address not found"));

        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Shipping address does not belong to user");
        }

        if (!billingAddress.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Billing address does not belong to user");
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        Orders savedOrder = ordersRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();

        // Process-Items
        for (OrderItemRequestDTO itemDTO : request.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Stock-validation
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Deduct-stock
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            BigDecimal price = product.getSellingPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtPurchase(price);

            orderItemsRepository.save(orderItem);

            totalAmount = totalAmount.add(subtotal);

            responseItems.add(
                    new OrderItemResponseDTO(
                            product.getProductId(),
                            product.getName(),
                            itemDTO.getQuantity(),
                            price,
                            subtotal));
        }

        savedOrder.setTotalAmount(totalAmount);
        Orders finalOrder = ordersRepository.save(savedOrder);

        return new OrderResponseDTO(
                finalOrder.getOrderId(),
                finalOrder.getOrderNumber(),
                finalOrder.getTotalAmount(),
                finalOrder.getStatus().name(),
                finalOrder.getOrderDate(),
                responseItems);
    }

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {

        List<Orders> orders = ordersRepository.findByUser_UserId(userId);

        List<OrderResponseDTO> responseList = new ArrayList<>();

        for (Orders order : orders) {

            List<OrderItemResponseDTO> items = new ArrayList<>();

            for (OrderItems item : order.getOrderItems()) {

                BigDecimal subtotal = item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                items.add(new OrderItemResponseDTO(
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        subtotal));
            }

            responseList.add(new OrderResponseDTO(
                    order.getOrderId(),
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    order.getOrderDate(),
                    items));
        }

        return responseList;
    }

    public void cancelOrder(Long orderId, Long userId) {

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == Orders.OrderStatus.SHIPPED ||
                order.getStatus() == Orders.OrderStatus.DELIVERED) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        order.setStatus(Orders.OrderStatus.CANCELLED);
        ordersRepository.save(order);

    }
}
