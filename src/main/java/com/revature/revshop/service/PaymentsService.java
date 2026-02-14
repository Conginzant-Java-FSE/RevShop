package com.revature.revshop.service;

import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Payments;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;

    @Autowired
    public PaymentsService(PaymentsRepository paymentsRepository, OrdersRepository ordersRepository) {
        this.paymentsRepository = paymentsRepository;
        this.ordersRepository = ordersRepository;
    }

    public Payments createPayment(Payments payment, Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        payment.setOrder(order);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        }
        return paymentsRepository.save(payment);
    }

    public Optional<Payments> getPaymentById(Integer paymentId) {
        return paymentsRepository.findById(paymentId);
    }

    public Optional<Payments> getPaymentByOrderId(Long orderId) {
        return paymentsRepository.findByOrder_OrderId(orderId);
    }

    public List<Payments> getPaymentsByStatus(Payments.PaymentStatus status) {
        return paymentsRepository.findByPaymentStatus(status);
    }

    public List<Payments> getPaymentsByMethod(Payments.PaymentMethod method) {
        return paymentsRepository.findByPaymentMethod(method);
    }

    public List<Payments> getAllPayments() {
        return paymentsRepository.findAll();
    }

    public Payments updatePaymentStatus(Integer paymentId, Payments.PaymentStatus status) {
        Payments payment = paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setPaymentStatus(status);
        return paymentsRepository.save(payment);
    }

    public void deletePayment(Integer paymentId) {
        if (!paymentsRepository.existsById(paymentId)) {
            throw new RuntimeException("Payment not found");
        }
        paymentsRepository.deleteById(paymentId);
    }
}
