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
    private static final String AMOUNT = "amount";
    private static final String CURRENCY = "currency";

    private final PaymentsService paymentsService;
    private final com.revature.revshop.service.OrdersService ordersService;

    public PaymentsController(PaymentsService paymentsService,
            com.revature.revshop.service.OrdersService ordersService) {
        this.paymentsService = paymentsService;
        this.ordersService = ordersService;
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

    // ─── Razorpay Integration ────────────────────────────────────────────────

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.id:test_key}")
    private String razorpayKeyId;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.secret:test_secret}")
    private String razorpayKeySecret;

    /**
     * POST /api/payments/create-order
     * Body: { "amount": 50000, "currency": "INR", "orderId": 123 }
     * amount should be in paise (rupees * 100)
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createRazorpayOrder(
            @RequestBody java.util.Map<String, Object> body) {

        long amountPaise = Long.parseLong(body.get(AMOUNT).toString());
        Long internalOrderId = Long.parseLong(body.get("orderId").toString());
        log.info("POST /api/payments/create-order - internalOrderId={} amount={}", internalOrderId, amountPaise);

        try {
            com.razorpay.RazorpayClient client = new com.razorpay.RazorpayClient(razorpayKeyId, razorpayKeySecret);

            org.json.JSONObject options = new org.json.JSONObject();
            options.put(AMOUNT, amountPaise);
            options.put(CURRENCY, body.getOrDefault(CURRENCY, "INR").toString());
            options.put("receipt", "rcpt_" + internalOrderId);

            com.razorpay.Order razorpayOrder = client.orders.create(options);

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("razorpayOrderId", razorpayOrder.get("id"));
            result.put(AMOUNT, amountPaise);
            result.put(CURRENCY, razorpayOrder.get(CURRENCY));
            result.put("keyId", razorpayKeyId);

            return ResponseEntity.ok(new ApiResponse<>("Razorpay order created", result));

        } catch (Exception e) {
            log.error("Failed to create Razorpay order for internalOrderId={}: {}", internalOrderId, e.getMessage(), e);
            throw new PaymentFailedException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    /**
     * POST /api/payments/verify
     * Body: { "razorpayOrderId": "...", "razorpayPaymentId": "...",
     * "razorpaySignature": "...", "internalOrderId": 123 }
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyRazorpayPayment(
            @RequestBody java.util.Map<String, Object> body) {

        String razorpayOrderId = body.get("razorpayOrderId").toString();
        String razorpayPaymentId = body.get("razorpayPaymentId").toString();
        String razorpaySignature = body.get("razorpaySignature").toString();
        Long internalOrderId = Long.parseLong(body.get("internalOrderId").toString());

        log.info("POST /api/payments/verify - internalOrderId={} paymentId={}", internalOrderId, razorpayPaymentId);

        try {
            // HMAC-SHA256 verification
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    razorpayKeySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexStr = new StringBuilder();
            for (byte b : hash)
                hexStr.append(String.format("%02x", b));
            String expectedSignature = hexStr.toString();

            if (!expectedSignature.equals(razorpaySignature)) {
                throw new PaymentFailedException("Payment signature verification failed");
            }

            // Mark payment as SUCCESS in DB
            paymentsService.getPaymentByOrderId(internalOrderId).ifPresent(payment -> {
                payment.setPaymentStatus(Payments.PaymentStatus.SUCCESS);
                payment.setTransactionId(razorpayPaymentId);
                paymentsService.savePayment(payment);

                // Update order status to PROCESSING and trigger confirmation email
                ordersService.updateOrderStatus(internalOrderId, "PROCESSING", 0L);
            });

            return ResponseEntity.ok(new ApiResponse<>("Payment verified successfully", true));

        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Payment verification failed for internalOrderId={} and paymentId={}: {}",
                    internalOrderId, razorpayPaymentId, e.getMessage(), e);
            throw new PaymentFailedException("Payment verification error: " + e.getMessage(), e);
        }
    }
}