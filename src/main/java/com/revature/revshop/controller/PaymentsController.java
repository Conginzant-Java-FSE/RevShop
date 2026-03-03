package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.PaymentsDTO;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Payments;
import com.revature.revshop.service.PaymentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

    private final PaymentsService paymentsService;

    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentsDTO>> createPayment(@RequestParam Long orderId,
            @RequestBody PaymentsDTO paymentsDTO) {

        log.info("POST /api/payments - orderId={}", orderId);
        Payments payment = convertToEntity(paymentsDTO);
        Payments savedPayment = paymentsService.createPayment(payment, orderId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Payment created successfully",
                        convertToDTO(savedPayment)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentsDTO>> getPaymentById(
            @PathVariable Integer id) {

        log.info("GET /api/payments/{}", id);
        Payments payment = paymentsService.getPaymentById(id)
                .orElseThrow(() -> new PaymentFailedException("Payment not found"));

        return ResponseEntity.ok(
                new ApiResponse<>("Payment fetched successfully",
                        convertToDTO(payment)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentsDTO>> getPaymentByOrderId(
            @PathVariable Long orderId) {

        log.info("GET /api/payments/order/{}", orderId);
        Payments payment = paymentsService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found for this order"));

        return ResponseEntity.ok(
                new ApiResponse<>("Payment fetched successfully",
                        convertToDTO(payment)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentsDTO>>> getAllPayments() {

        log.info("GET /api/payments");
        List<PaymentsDTO> payments = paymentsService.getAllPayments()
                .stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>("Payments fetched successfully", payments));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentsDTO> updatePaymentStatus(@PathVariable Integer id,
            @RequestParam String status) {
        log.info("PUT /api/payments/{}/status - status={}", id, status);
        Payments.PaymentStatus paymentStatus = Payments.PaymentStatus.valueOf(status);
        Payments updatedPayment = paymentsService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(convertToDTO(updatedPayment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Integer id) {

        log.info("DELETE /api/payments/{}", id);
        paymentsService.deletePayment(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Payment deleted successfully", null));
    }

    private PaymentsDTO convertToDTO(Payments payment) {

        PaymentsDTO dto = new PaymentsDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        if (payment.getOrder() != null)
            dto.setOrderId(payment.getOrder().getOrderId().intValue());

        if (payment.getPaymentMethod() != null)
            dto.setPaymentMethod(payment.getPaymentMethod().name());

        if (payment.getPaymentStatus() != null)
            dto.setPaymentStatus(payment.getPaymentStatus().name());

        return dto;
    }

    private Payments convertToEntity(PaymentsDTO dto) {

        Payments payment = new Payments();

        payment.setAmount(dto.getAmount());
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentDate(dto.getPaymentDate());

        if (dto.getPaymentMethod() != null)
            payment.setPaymentMethod(Payments.PaymentMethod.valueOf(dto.getPaymentMethod()));

        if (dto.getPaymentStatus() != null)
            payment.setPaymentStatus(Payments.PaymentStatus.valueOf(dto.getPaymentStatus()));

        return payment;
    }
}