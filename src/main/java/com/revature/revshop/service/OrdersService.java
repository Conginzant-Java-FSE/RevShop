package com.revature.revshop.service;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.*;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public OrdersService(OrdersRepository ordersRepository,
                         OrderItemsRepository orderItemsRepository,
                         UserRepository userRepository,
                         AddressRepository addressRepository,
                         ProductRepository productRepository,
                         NotificationService notificationService) {

        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public OrderResponseDTO placeOrder(Long userId, OrderRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));

        Address billingAddress = addressRepository.findById(request.getBillingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing address not found"));

        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new InvalidInputException("Shipping address does not belong to user");
        }

        if (!billingAddress.getUser().getUserId().equals(userId)) {
            throw new InvalidInputException("Billing address does not belong to user");
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Orders.OrderStatus.PENDING);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(BigDecimal.ZERO);

        Orders savedOrder = ordersRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();

        for (OrderItemRequestDTO itemDTO : request.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new InvalidInputException("Insufficient stock for product: " + product.getName());
            }

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

            responseItems.add(new OrderItemResponseDTO(
                    product.getProductId(),
                    product.getName(),
                    itemDTO.getQuantity(),
                    price,
                    subtotal));
        }

        savedOrder.setTotalAmount(totalAmount);
        Orders finalOrder = ordersRepository.save(savedOrder);

        notificationService.createNotification(
                userId,
                "Order Placed",
                "Your order " + finalOrder.getOrderNumber() + " has been placed successfully."
        );

        return new OrderResponseDTO(
                finalOrder.getOrderId(),
                finalOrder.getOrderNumber(),
                finalOrder.getTotalAmount(),
                finalOrder.getStatus().name(),
                finalOrder.getOrderDate(),
                responseItems);
    }

    public void cancelOrder(Long orderId, Long userId) {

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == Orders.OrderStatus.SHIPPED ||
                order.getStatus() == Orders.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Order cannot be cancelled");
        }

        order.setStatus(Orders.OrderStatus.CANCELLED);
        ordersRepository.save(order);

        notificationService.createNotification(
                userId,
                "Order Cancelled",
                "Your order " + order.getOrderNumber() + " has been cancelled."
        );
    }

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {

        List<Orders> orders = ordersRepository.findByUser_UserId(userId);

        return orders.stream().map(order -> new OrderResponseDTO(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getOrderDate(),
                new ArrayList<>()
        )).toList();
    }
}