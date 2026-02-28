package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.TrackingDetailsDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.TrackingDetails;
import com.revature.revshop.service.TrackingDetailsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingDetailsService trackingDetailsService;

    public TrackingController(TrackingDetailsService trackingDetailsService) {
        this.trackingDetailsService = trackingDetailsService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<TrackingDetailsDTO>> addTrackingDetail(
            @RequestParam Long orderId,
            @RequestBody TrackingDetailsDTO trackingDTO) {

        TrackingDetails tracking = convertToEntity(trackingDTO);

        TrackingDetails savedTracking =
                trackingDetailsService.addTrackingDetail(tracking, orderId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Tracking detail added successfully",
                        convertToDTO(savedTracking)
                ));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrackingDetailsDTO>> getTrackingById(
            @PathVariable Integer id) {

        TrackingDetails tracking =
                trackingDetailsService.getTrackingById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Tracking detail not found"));

        return ResponseEntity.ok(
                new ApiResponse<>("Tracking detail fetched successfully",
                        convertToDTO(tracking))
        );
    }


    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<TrackingDetailsDTO>>> getTrackingByOrderId(
            @PathVariable Long orderId) {

        List<TrackingDetailsDTO> list =
                trackingDetailsService.getTrackingByOrderId(orderId)
                        .stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Tracking details fetched successfully", list)
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TrackingDetailsDTO>>> getAllTrackingDetails() {

        List<TrackingDetailsDTO> list =
                trackingDetailsService.getAllTrackingDetails()
                        .stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("All tracking details fetched successfully", list)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrackingDetailsDTO>> updateTrackingStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            @RequestParam String description) {

        TrackingDetails updated =
                trackingDetailsService.updateTrackingStatus(id, status, description);

        return ResponseEntity.ok(
                new ApiResponse<>("Tracking status updated successfully",
                        convertToDTO(updated))
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTracking(
            @PathVariable Integer id) {

        trackingDetailsService.deleteTracking(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Tracking detail deleted successfully", null)
        );
    }


    private TrackingDetailsDTO convertToDTO(TrackingDetails tracking) {

        TrackingDetailsDTO dto = new TrackingDetailsDTO();

        dto.setTrackingId(tracking.getTrackingId());

        if (tracking.getOrder() != null)
            dto.setOrderId(tracking.getOrder().getOrderId().intValue());

        dto.setStatus(tracking.getStatus());
        dto.setDescription(tracking.getDescription());
        dto.setUpdatedAt(tracking.getUpdatedAt());
        dto.setCreatedAt(tracking.getCreatedAt());

        return dto;
    }

    private TrackingDetails convertToEntity(TrackingDetailsDTO dto) {

        TrackingDetails tracking = new TrackingDetails();
        tracking.setStatus(dto.getStatus());
        tracking.setDescription(dto.getDescription());

        return tracking;
    }
}