package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Payments;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.PaymentsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {

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

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

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

        Payments payment = paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found"));

        payment.setPaymentStatus(status);
        Payments updated = paymentsRepository.save(payment);

        notificationService.createNotification(
                updated.getOrder().getUser().getUserId(),
                "Payment Update",
                "Payment status updated to " + updated.getPaymentStatus().name()
        );

        return updated;
    }


    public Optional<Payments> getPaymentById(Integer paymentId) {
        return paymentsRepository.findById(paymentId);
    }

    public Optional<Payments> getPaymentByOrderId(Long orderId) {
        return paymentsRepository.findByOrder_OrderId(orderId);
    }

    public List<Payments> getAllPayments() {
        return paymentsRepository.findAll();
    }

    public void deletePayment(Integer paymentId) {
        if (!paymentsRepository.existsById(paymentId)) {
            throw new PaymentFailedException("Payment not found");
        }
        paymentsRepository.deleteById(paymentId);
    }
}