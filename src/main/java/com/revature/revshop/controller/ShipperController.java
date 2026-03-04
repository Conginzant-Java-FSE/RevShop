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

import java.util.List;

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

    @PostMapping("/{shipperId}/assign/{orderId}")
    public ResponseEntity<ApiResponse<Orders>> assignOrderToShipper(
            @PathVariable Long shipperId,
            @PathVariable Long orderId) {
        log.info("POST /api/shippers/{}/assign/{}", shipperId, orderId);
        Orders updated = shipperService.assignOrderToShipper(orderId, shipperId);
        return ResponseEntity.ok(new ApiResponse<>("Shipper assigned to order successfully", updated));
    }
}
