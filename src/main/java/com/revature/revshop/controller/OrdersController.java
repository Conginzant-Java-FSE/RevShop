package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.service.OrdersService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(
            @Valid @RequestBody OrderRequestDTO request) {

        log.info("POST /api/orders/place - userId={}", request.getUserId());

        OrderResponseDTO response = ordersService.placeOrder(request.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Order placed successfully",
                        response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getUserOrders(
            @PathVariable Long userId) {

        log.info("GET /api/orders/user/{}", userId);

        List<OrderResponseDTO> orders = ordersService.getOrdersByUser(userId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Orders fetched successfully",
                        orders));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequestDTO request) {

        log.info("PUT /api/orders/{}/cancel - userId={}", orderId, request.getUserId());

        ordersService.cancelOrder(orderId, request.getUserId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Order cancelled successfully",
                        null));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getSellerOrders(
            @PathVariable Long sellerId) {
        log.info("GET /api/orders/seller/{}", sellerId);
        List<OrderResponseDTO> orders = ordersService.getOrdersBySeller(sellerId);
        return ResponseEntity.ok(new ApiResponse<>("Seller orders fetched", orders));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        Long sellerId = Long.parseLong(body.getOrDefault("sellerId", "0"));
        log.info("PUT /api/orders/{}/status - status={}", orderId, status);
        OrderResponseDTO updated = ordersService.updateOrderStatus(orderId, status, sellerId);
        return ResponseEntity.ok(new ApiResponse<>("Order status updated", updated));
    }
}