package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Shipper;
import com.revature.revshop.service.ShipperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shippers")
public class ShipperController {

    private static final Logger log = LoggerFactory.getLogger(ShipperController.class);

    private final ShipperService shipperService;

    public ShipperController(ShipperService shipperService) {
        this.shipperService = shipperService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Shipper>>> getAllShippers() {
        log.info("GET /api/shippers");
        List<Shipper> shippers = shipperService.getAllShippers();
        return ResponseEntity.ok(new ApiResponse<>("Shippers fetched successfully", shippers));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Shipper>>> getAvailableShippers() {
        log.info("GET /api/shippers/available");
        List<Shipper> shippers = shipperService.getAvailableShippers();
        return ResponseEntity.ok(new ApiResponse<>("Available shippers fetched", shippers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Shipper>> createShipper(@RequestBody Shipper shipper) {
        log.info("POST /api/shippers - name={}", shipper.getName());
        Shipper created = shipperService.createShipper(shipper);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Shipper created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShipper(@PathVariable Long id) {
        log.info("DELETE /api/shippers/{}", id);
        shipperService.deleteShipper(id);
        return ResponseEntity.ok(new ApiResponse<>("Shipper deleted successfully", null));
    }

    /**
     * Assign an order to a shipper (called by seller). Returns a minimal DTO to
     * avoid JSON loops.
     */
    @PostMapping("/{shipperId}/assign/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignOrderToShipper(
            @PathVariable Long shipperId,
            @PathVariable Long orderId) {
        log.info("POST /api/shippers/{}/assign/{}", shipperId, orderId);
        Orders updated = shipperService.assignOrderToShipper(orderId, shipperId);
        Map<String, Object> result = Map.of(
                "orderId", updated.getOrderId(),
                "orderNumber", updated.getOrderNumber() != null ? updated.getOrderNumber() : "",
                "status", updated.getStatus().name(),
                "shipperId", shipperId);
        return ResponseEntity.ok(new ApiResponse<>("Shipper assigned to order successfully", result));
    }

    /**
     * Get all orders assigned to a shipper (for shipper dashboard).
     */
    @GetMapping("/{shipperId}/orders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOrdersByShipper(
            @PathVariable Long shipperId) {
        log.info("GET /api/shippers/{}/orders", shipperId);
        List<Orders> orders = shipperService.getOrdersByShipper(shipperId);
        List<Map<String, Object>> result = orders.stream().map(o -> {
            List<Map<String, Object>> items = o.getOrderItems() != null
                    ? o.getOrderItems().stream().map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("productName", item.getProduct() != null ? item.getProduct().getName() : "Unknown");
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("price", item.getPriceAtPurchase());
                        return itemMap;
                    }).collect(Collectors.toList())
                    : new java.util.ArrayList<>();

            Map<String, Object> addressMap = new HashMap<>();
            if (o.getShippingAddress() != null) {
                addressMap.put("addressLine", safeStr(o.getShippingAddress().getAddressLine()));
                addressMap.put("street", safeStr(o.getShippingAddress().getStreet()));
                addressMap.put("city", safeStr(o.getShippingAddress().getCity()));
                addressMap.put("state", safeStr(o.getShippingAddress().getState()));
                addressMap.put("zipCode", safeStr(o.getShippingAddress().getZipCode()));
            }

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", o.getOrderId());
            orderMap.put("orderNumber", o.getOrderNumber() != null ? o.getOrderNumber() : "");
            orderMap.put("status", o.getStatus().name());
            orderMap.put("totalAmount", o.getTotalAmount());
            orderMap.put("customerName", o.getUser() != null ? o.getUser().getName() : "");
            orderMap.put("customerPhone",
                    o.getUser() != null && o.getUser().getPhone() != null ? o.getUser().getPhone() : "");
            orderMap.put("shippingAddress", addressMap);
            orderMap.put("orderItems", items);
            orderMap.put("orderDate", o.getOrderDate() != null ? o.getOrderDate().toString() : "");
            return orderMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("Orders fetched successfully", result));
    }

    /**
     * Shipper updates order status (e.g., SHIPPED -> DELIVERED).
     */
    @PatchMapping("/{shipperId}/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(
            @PathVariable Long shipperId,
            @PathVariable Long orderId,
            @RequestParam String status) {
        log.info("PATCH /api/shippers/{}/orders/{}/status - status={}", shipperId, orderId, status);
        Orders updated = shipperService.updateOrderStatus(orderId, shipperId, status);
        Map<String, Object> result = Map.of(
                "orderId", updated.getOrderId(),
                "status", updated.getStatus().name());
        return ResponseEntity.ok(new ApiResponse<>("Order status updated successfully", result));
    }

    /**
     * Toggle shipper availability.
     */
    @PatchMapping("/{shipperId}/availability")
    public ResponseEntity<ApiResponse<Shipper>> updateAvailability(
            @PathVariable Long shipperId,
            @RequestParam Boolean available) {
        log.info("PATCH /api/shippers/{}/availability - available={}", shipperId, available);
        Shipper updated = shipperService.updateAvailability(shipperId, available);
        return ResponseEntity.ok(new ApiResponse<>("Availability updated", updated));
    }

    private String safeStr(String s) {
        return s != null ? s : "";
    }
}
