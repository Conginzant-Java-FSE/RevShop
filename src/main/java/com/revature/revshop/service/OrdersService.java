package com.revature.revshop.service;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.*;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdersService {

    private static final Logger log = LoggerFactory.getLogger(OrdersService.class);
    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String SHIPPING_ADDRESS_NOT_FOUND = "Shipping address not found";
    private static final String BILLING_ADDRESS_NOT_FOUND = "Billing address not found";

    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final PaymentsRepository paymentsRepository;
    private final TrackingDetailsRepository trackingDetailsRepository;
    private final OrderItemService orderItemService;
    private final EmailService emailService;

    public OrdersService(OrdersRepository ordersRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository,
            NotificationService notificationService,
            PaymentsRepository paymentsRepository,
            TrackingDetailsRepository trackingDetailsRepository,
            OrderItemService orderItemService,
            EmailService emailService) {
        this.ordersRepository = ordersRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.paymentsRepository = paymentsRepository;
        this.trackingDetailsRepository = trackingDetailsRepository;
        this.orderItemService = orderItemService;
        this.emailService = emailService;
    }

    public OrderResponseDTO placeOrder(Long userId, OrderRequestDTO request) {
        log.info("Placing order for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(SHIPPING_ADDRESS_NOT_FOUND));

        Address billingAddress = addressRepository.findById(request.getBillingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(BILLING_ADDRESS_NOT_FOUND));

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
        order.setPaymentMethod(request.getPaymentMethod());

        Orders savedOrder = ordersRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();
        totalAmount = processOrderItems(request.getItems(), savedOrder, responseItems);

        savedOrder.setTotalAmount(totalAmount);
        Orders finalOrder = ordersRepository.save(savedOrder);

        notificationService.createNotification(
                userId,
                "Order Placed",
                "Your order " + finalOrder.getOrderNumber() + " has been placed successfully.");

        createTrackingDetail(finalOrder, finalOrder.getStatus().name(),
                "Order placed successfully. Waiting for processing.");

        try {
            emailService.sendOrderConfirmation(finalOrder, user.getEmail());
        } catch (Exception e) {
            log.warn("Email send failed", e);
        }

        processPaymentAndTracking(finalOrder, totalAmount, request.getPaymentMethod());

        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(finalOrder.getOrderId());
        response.setOrderNumber(finalOrder.getOrderNumber());
        response.setTotalAmount(finalOrder.getTotalAmount());
        response.setStatus(finalOrder.getStatus().name());
        response.setOrderDate(finalOrder.getOrderDate());
        response.setPaymentMethod(finalOrder.getPaymentMethod());
        response.setBuyerName(user.getName());
        response.setBuyerEmail(user.getEmail());
        response.setItems(responseItems);
        return response;
    }

    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order id={} userId={}", orderId, userId);
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == Orders.OrderStatus.SHIPPED || order.getStatus() == Orders.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Order cannot be cancelled");
        }

        order.setStatus(Orders.OrderStatus.CANCELLED);
        Orders savedOrder = ordersRepository.save(order);
        createTrackingDetail(savedOrder, "CANCELLED", "Order has been cancelled.");

        notificationService.createNotification(
                userId,
                "Order Cancelled",
                "Your order " + order.getOrderNumber() + " has been cancelled.");
    }

    public void requestReturn(Long orderId, Long userId, String reason) {
        log.info("Requesting return for orderId={} userId={}", orderId, userId);
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to return this order");
        }

        if (order.getStatus() != Orders.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Only delivered orders can be returned");
        }

        order.setStatus(Orders.OrderStatus.RETURN_REQUESTED);
        Orders savedOrder = ordersRepository.save(order);
        createTrackingDetail(savedOrder, "RETURN_REQUESTED", "Return requested: " + reason);

        notificationService.createNotification(
                userId,
                "Return Requested",
                "Your return for order " + order.getOrderNumber() + " has been submitted.");
    }

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        log.info("Fetching orders for userId={}", userId);
        List<Orders> orders = ordersRepository.findByUserUserId(userId);
        return orders.stream().map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setOrderId(order.getOrderId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus().name());
            dto.setOrderDate(order.getOrderDate());
            dto.setPaymentMethod(order.getPaymentMethod());
            dto.setBuyerName(order.getUser().getName());
            dto.setBuyerEmail(order.getUser().getEmail());
            dto.setItems(new ArrayList<>());
            return dto;
        }).toList();
    }

    public List<OrderResponseDTO> getOrdersBySeller(Long sellerId) {
        List<Orders> orders = ordersRepository.findOrdersBySellerId(sellerId);
        return orders.stream().map(order -> {
            List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                    .filter(oi -> oi.getProduct().getSeller() != null &&
                            oi.getProduct().getSeller().getUser().getUserId().equals(sellerId))
                    .map(oi -> new OrderItemResponseDTO(
                            oi.getProduct().getProductId(),
                            oi.getProduct().getName(),
                            oi.getQuantity(),
                            oi.getPriceAtPurchase(),
                            oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(oi.getQuantity()))))
                    .toList();
            String shipperName = (order.getShipper() != null) ? order.getShipper().getName() : null;
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setOrderId(order.getOrderId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus().name());
            dto.setOrderDate(order.getOrderDate());
            dto.setPaymentMethod(order.getPaymentMethod());
            dto.setBuyerName(order.getUser().getName());
            dto.setBuyerEmail(order.getUser().getEmail());
            dto.setItems(items);
            dto.setShipperName(shipperName);
            return dto;
        }).toList();
    }

    public OrderResponseDTO updateOrderStatus(Long orderId, String status, Long sellerId) {
        log.info("Updating order id={} status={}", orderId, status);
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        Orders.OrderStatus newStatus;
        try {
            newStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid order status: " + status);
        }

        order.setStatus(newStatus);
        Orders saved = ordersRepository.save(order);
        createTrackingDetail(saved, newStatus.name(), getTrackingDescriptionForStatus(newStatus));

        notificationService.createNotification(
                saved.getUser().getUserId(),
                "Order Status Updated",
                "Your order " + saved.getOrderNumber() + " is now " + newStatus.name() + ".");

        if (newStatus == Orders.OrderStatus.SHIPPED) {
            try {
                emailService.sendShippingNotification(saved, saved.getUser().getEmail());
            } catch (Exception e) {
                log.warn("Email send failed", e);
            }
        } else {
            try {
                emailService.sendStatusUpdate(saved, saved.getUser().getEmail(), newStatus.name());
            } catch (Exception e) {
                log.warn("Email send failed", e);
            }
        }

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(saved.getOrderId());
        dto.setOrderNumber(saved.getOrderNumber());
        dto.setTotalAmount(saved.getTotalAmount());
        dto.setStatus(saved.getStatus().name());
        dto.setOrderDate(saved.getOrderDate());
        dto.setPaymentMethod(saved.getPaymentMethod());
        dto.setBuyerName(saved.getUser().getName());
        dto.setBuyerEmail(saved.getUser().getEmail());
        dto.setItems(new ArrayList<>());
        return dto;
    }

    public OrderResponseDTO getOrderById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        List<OrderItemResponseDTO> responseItems = order.getOrderItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setBuyerName(order.getUser().getName());
        dto.setBuyerEmail(order.getUser().getEmail());
        dto.setItems(responseItems);
        return dto;
    }

    public List<TrackingDetailsDTO> getOrderTracking(Long orderId) {
        return trackingDetailsRepository.findByOrderOrderId(orderId).stream()
                .map(t -> {
                    TrackingDetailsDTO dto = new TrackingDetailsDTO();
                    dto.setTrackingId(t.getTrackingId());
                    dto.setOrderId(t.getOrder().getOrderId().intValue());
                    dto.setStatus(t.getStatus());
                    dto.setDescription(t.getDescription());
                    dto.setUpdatedAt(t.getUpdatedAt());
                    dto.setCreatedAt(t.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    private void createTrackingDetail(Orders order, String status, String description) {
        TrackingDetails tracking = new TrackingDetails();
        tracking.setOrder(order);
        tracking.setStatus(status);
        tracking.setDescription(description);
        trackingDetailsRepository.save(tracking);
    }

    private String getTrackingDescriptionForStatus(Orders.OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Order placed and pending approval.";
            case PROCESSING:
                return "Order is being processed and packed.";
            case SHIPPED:
                return "Order has been shipped and is on its way.";
            case OUT_FOR_DELIVERY:
                return "Order is out for delivery and will reach you soon.";
            case DELIVERED:
                return "Order has been delivered successfully.";
            case CANCELLED:
                return "Order has been cancelled.";
            case RETURN_REQUESTED:
                return "Return request has been submitted.";
            case RETURN_APPROVED:
                return "Return request has been approved.";
            case RETURN_REJECTED:
                return "Return request has been rejected.";
            default:
                return "Order status updated to " + status.name();

        }
    }

    private BigDecimal processOrderItems(List<OrderItemRequestDTO> items, Orders order,
            List<OrderItemResponseDTO> responseItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequestDTO itemDTO : items) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new InvalidInputException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            Product savedProduct = productRepository.save(product);

            if (savedProduct.getStockQuantity() < savedProduct.getThresholdQuantity()) {
                notifySellerLowStock(savedProduct);
            }

            BigDecimal price = product.getSellingPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            orderItemService.createOrderItem(order, product, itemDTO.getQuantity());
            totalAmount = totalAmount.add(subtotal);

            responseItems.add(new OrderItemResponseDTO(
                    product.getProductId(),
                    product.getName(),
                    itemDTO.getQuantity(),
                    price,
                    subtotal));

            notifySellerNewOrder(product, itemDTO.getQuantity());
        }
        return totalAmount;
    }

    private void notifySellerLowStock(Product product) {
        if (product.getSeller() != null && product.getSeller().getUser() != null) {
            notificationService.createNotification(
                    product.getSeller().getUser().getUserId(),
                    "Low Stock Alert",
                    "Product '" + product.getName() + "' has low stock: "
                            + product.getStockQuantity()
                            + " units remaining.");
        }
    }

    private void notifySellerNewOrder(Product product, int quantity) {
        if (product.getSeller() != null && product.getSeller().getUser() != null) {
            notificationService.createNotification(
                    product.getSeller().getUser().getUserId(),
                    "New Order Received",
                    "Your product '" + product.getName() + "' was ordered (Qty: "
                            + quantity + ").");
        }
    }

    private void processPaymentAndTracking(Orders order, BigDecimal amount, String paymentMethod) {
        String rawMethod = paymentMethod != null ? paymentMethod.toUpperCase() : "COD";
        Payments.PaymentMethod payMethod;
        try {
            payMethod = Payments.PaymentMethod.valueOf(rawMethod);
        } catch (IllegalArgumentException e) {
            payMethod = Payments.PaymentMethod.COD;
        }

        boolean isPending = (payMethod == Payments.PaymentMethod.COD ||
                payMethod == Payments.PaymentMethod.RAZORPAY);
        String txnId = isPending ? null : "TXN-" + System.currentTimeMillis() + "-" + order.getOrderId();

        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(payMethod);
        payment.setPaymentStatus(isPending ? Payments.PaymentStatus.PENDING : Payments.PaymentStatus.SUCCESS);
        payment.setTransactionId(txnId);
        payment.setPaymentDate(LocalDateTime.now());
        paymentsRepository.save(payment);

        createTrackingDetail(order, "PENDING", "Order placed successfully. Waiting for processing.");
    }

    public Map<String, Object> getSellerStats(Long sellerId) {
        List<Orders> sellerOrders = ordersRepository.findOrdersBySellerId(sellerId);

        BigDecimal totalRevenue = sellerOrders.stream()
                .map(Orders::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> ordersByStatus = sellerOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        Map<String, Integer> productUnits = new HashMap<>();
        for (Orders order : sellerOrders) {
            if (order.getOrderItems() == null)
                continue;
            for (OrderItems item : order.getOrderItems()) {
                if (item.getProduct() != null
                        && item.getProduct().getSeller() != null
                        && item.getProduct().getSeller().getUser().getUserId().equals(sellerId)) {
                    productUnits.merge(item.getProduct().getName(), item.getQuantity(), Integer::sum);
                }
            }
        }

        List<Map<String, Object>> topProducts = productUnits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("productName", e.getKey());
                    entry.put("unitsSold", e.getValue());
                    return entry;
                })
                .toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", sellerOrders.size());
        stats.put("ordersByStatus", ordersByStatus);
        stats.put("topProducts", topProducts);
        return stats;
    }
}
