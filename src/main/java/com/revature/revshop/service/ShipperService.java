package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Shipper;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShipperService {

    private static final Logger log = LoggerFactory.getLogger(ShipperService.class);

    private final ShipperRepository shipperRepository;
    private final OrdersRepository ordersRepository;
    private final NotificationService notificationService;

    public ShipperService(ShipperRepository shipperRepository,
            OrdersRepository ordersRepository,
            NotificationService notificationService) {
        this.shipperRepository = shipperRepository;
        this.ordersRepository = ordersRepository;
        this.notificationService = notificationService;
    }

    public List<Shipper> getAllShippers() {
        return shipperRepository.findAll();
    }

    public List<Shipper> getAvailableShippers() {
        return shipperRepository.findByIsAvailable(true);
    }

    public Shipper createShipper(Shipper shipper) {
        return shipperRepository.save(shipper);
    }

    public void deleteShipper(Long id) {
        if (!shipperRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipper not found with id: " + id);
        }
        shipperRepository.deleteById(id);
    }

    public Orders assignOrderToShipper(Long orderId, Long shipperId) {
        log.info("Assigning orderId={} to shipperId={}", orderId, shipperId);

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));

        order.setShipper(shipper);
        shipper.setIsAvailable(false);

        shipperRepository.save(shipper);
        Orders saved = ordersRepository.save(order);

        // Notify the buyer
        notificationService.createNotification(
                order.getUser().getUserId(),
                "Shipper Assigned",
                "Your order " + order.getOrderNumber() + " has been assigned to shipper "
                        + shipper.getName() + " and will be picked up shortly.");

        return saved;
    }
}
