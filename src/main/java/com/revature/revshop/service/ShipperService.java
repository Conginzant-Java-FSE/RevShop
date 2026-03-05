package com.revature.revshop.service;

import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Shipper;
import com.revature.revshop.model.TrackingDetails;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.ShipperRepository;
import com.revature.revshop.repository.TrackingDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShipperService {

    private static final Logger log = LoggerFactory.getLogger(ShipperService.class);

    private final ShipperRepository shipperRepository;
    private final OrdersRepository ordersRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final TrackingDetailsRepository trackingDetailsRepository;

    public ShipperService(ShipperRepository shipperRepository,
            OrdersRepository ordersRepository,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            TrackingDetailsRepository trackingDetailsRepository) {
        this.shipperRepository = shipperRepository;
        this.ordersRepository = ordersRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.trackingDetailsRepository = trackingDetailsRepository;
    }

    public List<Shipper> getAllShippers() {
        return shipperRepository.findAll();
    }

    public List<Shipper> getAvailableShippers() {
        return shipperRepository.findByIsAvailable(true);
    }

    public Shipper createShipper(Shipper shipper) {
        if (shipper.getPassword() != null && !shipper.getPassword().isEmpty()) {
            shipper.setPassword(passwordEncoder.encode(shipper.getPassword()));
        }
        return shipperRepository.save(shipper);
    }

    public boolean existsByEmail(String email) {
        return shipperRepository.findByEmail(email).isPresent();
    }

    /**
     * Register a new shipper with validation.
     */
    public Shipper registerShipper(String name, String email, String phone,
            String vehicleNumber, String password) {
        if (existsByEmail(email)) {
            throw new com.revature.revshop.exception.InvalidInputException(
                    "Shipper with this email already exists");
        }
        Shipper shipper = new Shipper();
        shipper.setName(name);
        shipper.setEmail(email);
        shipper.setPhone(phone);
        shipper.setVehicleNumber(vehicleNumber);
        shipper.setIsAvailable(true);
        shipper.setPassword(passwordEncoder.encode(password));
        return shipperRepository.save(shipper);
    }

    public void deleteShipper(Long id) {
        if (!shipperRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipper not found with id: " + id);
        }
        shipperRepository.deleteById(id);
    }

    /**
     * Authenticate a shipper by email and password.
     */
    public Optional<Shipper> loginShipper(String email, String password) {
        Optional<Shipper> shipperOpt = shipperRepository.findByEmail(email);
        if (shipperOpt.isEmpty()) {
            return Optional.empty();
        }
        Shipper shipper = shipperOpt.get();
        if (shipper.getPassword() == null || !passwordEncoder.matches(password, shipper.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(shipper);
    }

    /**
     * Assign an order to a shipper.
     */
    public Orders assignOrderToShipper(Long orderId, Long shipperId) {
        log.info("Assigning orderId={} to shipperId={}", orderId, shipperId);

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));

        order.setShipper(shipper);
        order.setStatus(Orders.OrderStatus.SHIPPED);
        // NOTE: Availability is manually controlled by the shipper — do NOT auto-change
        // it here.

        Orders saved = ordersRepository.save(order);

        // Notify the buyer
        notificationService.createNotification(
                order.getUser().getUserId(),
                "Shipper Assigned",
                "Your order " + order.getOrderNumber() + " has been assigned to shipper "
                        + shipper.getName() + " and will be picked up shortly.");

        // Record tracking
        createTrackingDetail(saved, "SHIPPED", "Order has been assigned to a shipper and is on the way.");

        return saved;
    }

    /**
     * Get all orders assigned to a specific shipper.
     */
    public List<Orders> getOrdersByShipper(Long shipperId) {
        return ordersRepository.findByShipper_ShipperId(shipperId);
    }

    /**
     * Update delivery status of an order by the shipper.
     */
    public Orders updateOrderStatus(Long orderId, Long shipperId, String status) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getShipper() == null || !order.getShipper().getShipperId().equals(shipperId)) {
            throw new InvalidInputException("This order is not assigned to you");
        }

        Orders.OrderStatus newStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        if (newStatus == Orders.OrderStatus.DELIVERED) {
            // Notify buyer on successful delivery
            notificationService.createNotification(
                    order.getUser().getUserId(),
                    "Order Delivered!",
                    "Your order " + order.getOrderNumber() + " has been delivered successfully.");
            // NOTE: Availability is manually controlled by the shipper — do NOT auto-change
            // it here.
        }

        Orders savedOrder = ordersRepository.save(order);

        // Add tracking detail entry
        String description = "Update from Shipper: Order is now " + status;
        if (newStatus == Orders.OrderStatus.SHIPPED) {
            description = "Order has been dispatched and is on the way.";
        } else if (newStatus == Orders.OrderStatus.OUT_FOR_DELIVERY) {
            description = "Order is out for delivery and will reach you soon.";
        } else if (newStatus == Orders.OrderStatus.DELIVERED) {
            description = "Order has been delivered successfully.";
        }
        createTrackingDetail(savedOrder, status.toUpperCase(), description);

        return savedOrder;
    }

    /**
     * Toggle shipper availability.
     */
    public Shipper updateAvailability(Long shipperId, Boolean available) {
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));
        shipper.setIsAvailable(available);
        return shipperRepository.save(shipper);
    }

    private void createTrackingDetail(Orders order, String status, String description) {
        TrackingDetails tracking = new TrackingDetails();
        tracking.setOrder(order);
        tracking.setStatus(status);
        tracking.setDescription(description);
        trackingDetailsRepository.save(tracking);
    }
}
