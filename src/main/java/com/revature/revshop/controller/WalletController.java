package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.model.User;
import com.revature.revshop.model.Wallet;
import com.revature.revshop.model.WalletTransaction;
import com.revature.revshop.service.UserService;
import com.revature.revshop.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @Autowired
    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        User loggedInUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return loggedInUser.getUserId();
    }

    @PostMapping("/kyc/send-sms")
    public ResponseEntity<ApiResponse<String>> sendSmsOtp(@RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        walletService.sendSmsOtp(getAuthenticatedUserId(), mobileNumber);
        return ResponseEntity.ok(new ApiResponse<>("SMS OTP sent successfully to " + mobileNumber, null));
    }

    @PostMapping("/kyc/verify")
    public ResponseEntity<ApiResponse<Wallet>> verifyMobileKyc(@RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        String otp = request.get("otp");
        Wallet wallet = walletService.verifyMobileKyc(getAuthenticatedUserId(), mobileNumber, otp);
        return ResponseEntity.ok(new ApiResponse<>("Wallet created and KYC verified successfully", wallet));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Wallet>> getWalletBalance() {
        Wallet wallet = walletService.getWalletByUser(getAuthenticatedUserId());
        return ResponseEntity.ok(new ApiResponse<>("Wallet fetched successfully", wallet));
    }

    @PostMapping("/create-razorpay-order")
    public ResponseEntity<ApiResponse<String>> createRazorpayOrder(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String orderId = walletService.createRazorpayOrder(amount);
            return ResponseEntity.ok(new ApiResponse<>("Razorpay order created", orderId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<Wallet>> verifyPaymentAndAddMoney(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String razorpayPaymentId = (String) request.get("razorpayPaymentId");
        String razorpayOrderId = (String) request.get("razorpayOrderId");
        String razorpaySignature = (String) request.get("razorpaySignature");

        Wallet wallet = walletService.verifyPaymentAndAddMoney(
                getAuthenticatedUserId(), amount, razorpayPaymentId, razorpayOrderId, razorpaySignature);
        return ResponseEntity.ok(new ApiResponse<>("Payment verified and money added successfully", wallet));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getWalletTransactions() {
        List<WalletTransaction> transactions = walletService.getWalletTransactions(getAuthenticatedUserId());
        return ResponseEntity.ok(new ApiResponse<>("Wallet transactions fetched successfully", transactions));
    }
}
