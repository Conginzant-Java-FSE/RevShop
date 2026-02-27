package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.PaymentsDTO;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Payments;
import com.revature.revshop.service.PaymentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final PaymentsService paymentsService;

    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }


    @PostMapping
    public ResponseEntity<PaymentsDTO> createPayment(@RequestParam Long orderId,
            @RequestBody PaymentsDTO paymentsDTO) {
        Payments payment = convertToEntity(paymentsDTO);
        Payments savedPayment = paymentsService.createPayment(payment, orderId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Payment created successfully",
                        convertToDTO(savedPayment)
                ));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentsDTO>> getPaymentById(
            @PathVariable Integer id) {

        Payments payment = paymentsService.getPaymentById(id)
                .orElseThrow(() -> new PaymentFailedException("Payment not found"));

        return ResponseEntity.ok(
                new ApiResponse<>("Payment fetched successfully",
                        convertToDTO(payment))
        );
    }


    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentsDTO>> getPaymentByOrderId(
            @PathVariable Long orderId) {

        Payments payment = paymentsService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new PaymentFailedException("Payment not found for this order"));

        return ResponseEntity.ok(
                new ApiResponse<>("Payment fetched successfully",
                        convertToDTO(payment))
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentsDTO>>> getAllPayments() {

        List<PaymentsDTO> payments = paymentsService.getAllPayments()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Payments fetched successfully", payments)
        );
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentsDTO> updatePaymentStatus(@PathVariable Integer id,
            @RequestParam String status) {
        Payments.PaymentStatus paymentStatus = Payments.PaymentStatus.valueOf(status);
        Payments updatedPayment = paymentsService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(convertToDTO(updatedPayment));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Integer id) {

        paymentsService.deletePayment(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Payment deleted successfully", null)
        );
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