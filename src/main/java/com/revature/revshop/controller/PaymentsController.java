package com.revature.revshop.controller;

import com.revature.revshop.dto.PaymentsDTO;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Payments;
import com.revature.revshop.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final PaymentsService paymentsService;

    @Autowired
    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @PostMapping
    public ResponseEntity<PaymentsDTO> createPayment(@RequestParam Long orderId,
            @RequestBody PaymentsDTO paymentsDTO) {
        Payments payment = convertToEntity(paymentsDTO);
        Payments savedPayment = paymentsService.createPayment(payment, orderId);
        return ResponseEntity.ok(convertToDTO(savedPayment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentsDTO> getPaymentById(@PathVariable Integer id) {
        Payments payment = paymentsService.getPaymentById(id)
                .orElseThrow(() -> new PaymentFailedException("Payment not found"));
        return ResponseEntity.ok(convertToDTO(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentsDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        Payments payment = paymentsService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found for this order"));
        return ResponseEntity.ok(convertToDTO(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentsDTO>> getAllPayments() {
        List<Payments> payments = paymentsService.getAllPayments();
        return ResponseEntity.ok(payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentsDTO> updatePaymentStatus(@PathVariable Integer id,
            @RequestParam String status) {
        Payments.PaymentStatus paymentStatus = Payments.PaymentStatus.valueOf(status);
        Payments updatedPayment = paymentsService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(convertToDTO(updatedPayment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        paymentsService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    private PaymentsDTO convertToDTO(Payments payment) {
        PaymentsDTO dto = new PaymentsDTO();
        dto.setPaymentId(payment.getPaymentId());
        if (payment.getOrder() != null) {
            dto.setOrderId(payment.getOrder().getOrderId().intValue());
        }
        dto.setAmount(payment.getAmount());
        if (payment.getPaymentMethod() != null) {
            dto.setPaymentMethod(payment.getPaymentMethod().name());
        }
        if (payment.getPaymentStatus() != null) {
            dto.setPaymentStatus(payment.getPaymentStatus().name());
        }
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        return dto;
    }

    private Payments convertToEntity(PaymentsDTO dto) {
        Payments payment = new Payments();
        payment.setAmount(dto.getAmount());
        if (dto.getPaymentMethod() != null) {
            payment.setPaymentMethod(Payments.PaymentMethod.valueOf(dto.getPaymentMethod()));
        }
        if (dto.getPaymentStatus() != null) {
            payment.setPaymentStatus(Payments.PaymentStatus.valueOf(dto.getPaymentStatus()));
        }
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentDate(dto.getPaymentDate());
        return payment;
    }
}
