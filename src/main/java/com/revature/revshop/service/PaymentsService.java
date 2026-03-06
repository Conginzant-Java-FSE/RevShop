package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Payments;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.PaymentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {

    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);
    private static final String ORDER_NOT_FOUND = "Order not found";

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;
    private final NotificationService notificationService;

    public PaymentsService(PaymentsRepository paymentsRepository,
            OrdersRepository ordersRepository,
            NotificationService notificationService) {
        this.paymentsRepository = paymentsRepository;
        this.ordersRepository = ordersRepository;
        this.notificationService = notificationService;
    }

    public Payments createPayment(Payments payment, Long orderId) {
        log.info("Creating payment for orderId={}", orderId);

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        payment.setOrder(order);

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        }

        return paymentsRepository.save(payment);
    }

    public Payments updatePaymentStatus(Integer paymentId, Payments.PaymentStatus status) {
        log.info("Updating payment id={} status={}", paymentId, status);

        Payments payment = paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found"));

        payment.setPaymentStatus(status);
        Payments updated = paymentsRepository.save(payment);

        notificationService.createNotification(
                updated.getOrder().getUser().getUserId(),
                "Payment Update",
                "Payment status updated to " + updated.getPaymentStatus().name());

        return updated;
    }

    public Optional<Payments> getPaymentById(Integer paymentId) {
        return paymentsRepository.findById(paymentId);
    }

    public Optional<Payments> getPaymentByOrderId(Long orderId) {
        return paymentsRepository.findByOrderOrderId(orderId);
    }

    public List<Payments> getAllPayments() {
        return paymentsRepository.findAll();
    }

    public void deletePayment(Integer paymentId) {
        log.info("Deleting payment id={}", paymentId);
        if (!paymentsRepository.existsById(paymentId)) {
            throw new PaymentFailedException("Payment not found");
        }
        paymentsRepository.deleteById(paymentId);
    }

    public Payments savePayment(Payments payment) {
        log.info("Saving payment id={}", payment.getPaymentId());
        return paymentsRepository.save(payment);
    }
}
