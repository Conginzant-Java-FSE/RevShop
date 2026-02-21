package com.revature.revshop.controller;

import com.revature.revshop.dto.CancelOrderRequestDTO;
import com.revature.revshop.dto.OrderRequestDTO;
import com.revature.revshop.dto.OrderResponseDTO;
import com.revature.revshop.service.OrdersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            @Valid @RequestBody OrderRequestDTO request) {

        OrderResponseDTO response =
                ordersService.placeOrder(request.getUserId(), request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            @PathVariable Long userId) {

        List<OrderResponseDTO> orders =
                ordersService.getOrdersByUser(userId);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequestDTO request) {

        ordersService.cancelOrder(orderId, request.getUserId());

        return ResponseEntity.ok("Order cancelled successfully");
    }

}
