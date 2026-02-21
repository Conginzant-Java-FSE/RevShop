package com.revature.revshop.controller;

import com.revature.revshop.dto.TrackingDetailsDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.TrackingDetails;
import com.revature.revshop.service.TrackingDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingDetailsService trackingDetailsService;

    @Autowired
    public TrackingController(TrackingDetailsService trackingDetailsService) {
        this.trackingDetailsService = trackingDetailsService;
    }

    @PostMapping
    public ResponseEntity<TrackingDetailsDTO> addTrackingDetail(@RequestParam Long orderId,
                                                                @RequestBody TrackingDetailsDTO trackingDTO) {
        TrackingDetails tracking = convertToEntity(trackingDTO);
        TrackingDetails savedTracking = trackingDetailsService.addTrackingDetail(tracking, orderId);
        return ResponseEntity.ok(convertToDTO(savedTracking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackingDetailsDTO> getTrackingById(@PathVariable Integer id) {
        TrackingDetails tracking = trackingDetailsService.getTrackingById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking detail not found"));
        return ResponseEntity.ok(convertToDTO(tracking));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<TrackingDetailsDTO>> getTrackingByOrderId(@PathVariable Long orderId) {
        List<TrackingDetails> trackingList = trackingDetailsService.getTrackingByOrderId(orderId);
        return ResponseEntity.ok(trackingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<TrackingDetailsDTO>> getAllTrackingDetails() {
        List<TrackingDetails> trackingList = trackingDetailsService.getAllTrackingDetails();
        return ResponseEntity.ok(trackingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackingDetailsDTO> updateTrackingStatus(@PathVariable Integer id,
                                                                   @RequestParam String status, @RequestParam String description) {
        TrackingDetails updatedTracking = trackingDetailsService.updateTrackingStatus(id, status, description);
        return ResponseEntity.ok(convertToDTO(updatedTracking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTracking(@PathVariable Integer id) {
        trackingDetailsService.deleteTracking(id);
        return ResponseEntity.noContent().build();
    }

    private TrackingDetailsDTO convertToDTO(TrackingDetails tracking) {
        TrackingDetailsDTO dto = new TrackingDetailsDTO();
        dto.setTrackingId(tracking.getTrackingId());
        if (tracking.getOrder() != null) {
            dto.setOrderId(tracking.getOrder().getOrderId().intValue());
        }
        dto.setStatus(tracking.getStatus());
        dto.setDescription(tracking.getDescription());
        dto.setUpdatedAt(tracking.getUpdatedAt());
        return dto;
    }

    private TrackingDetails convertToEntity(TrackingDetailsDTO dto) {
        TrackingDetails tracking = new TrackingDetails();
        tracking.setStatus(dto.getStatus());
        tracking.setDescription(dto.getDescription());
        return tracking;
    }
}
