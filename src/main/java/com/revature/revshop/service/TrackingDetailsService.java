package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.TrackingDetails;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.TrackingDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrackingDetailsService {

    private static final Logger log = LoggerFactory.getLogger(TrackingDetailsService.class);

    private final TrackingDetailsRepository trackingDetailsRepository;
    private final OrdersRepository ordersRepository;

    @Autowired
    public TrackingDetailsService(TrackingDetailsRepository trackingDetailsRepository,
            OrdersRepository ordersRepository) {
        this.trackingDetailsRepository = trackingDetailsRepository;
        this.ordersRepository = ordersRepository;
    }

    public TrackingDetails addTrackingDetail(TrackingDetails trackingDetails, Long orderId) {
        log.info("Adding tracking detail for orderId={}", orderId);
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        trackingDetails.setOrder(order);
        return trackingDetailsRepository.save(trackingDetails);
    }

    public Optional<TrackingDetails> getTrackingById(Integer trackingId) {
        return trackingDetailsRepository.findById(trackingId);
    }

    public List<TrackingDetails> getTrackingByOrderId(Long orderId) {
        return trackingDetailsRepository.findByOrder_OrderId(orderId);
    }

    public List<TrackingDetails> getAllTrackingDetails() {
        return trackingDetailsRepository.findAll();
    }

    public TrackingDetails updateTrackingStatus(Integer trackingId, String status, String description) {
        log.info("Updating tracking id={} status={}", trackingId, status);
        TrackingDetails tracking = trackingDetailsRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking detail not found"));
        tracking.setStatus(status);
        tracking.setDescription(description);
        return trackingDetailsRepository.save(tracking);
    }

    public void deleteTracking(Integer trackingId) {
        log.info("Deleting tracking id={}", trackingId);
        if (!trackingDetailsRepository.existsById(trackingId)) {
            throw new ResourceNotFoundException("Tracking detail not found");
        }
        trackingDetailsRepository.deleteById(trackingId);
    }
}
