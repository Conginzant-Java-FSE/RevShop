package com.revature.revshop.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.revature.revshop.model.User;
import com.revature.revshop.model.Wallet;
import com.revature.revshop.model.WalletTransaction;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.repository.WalletRepository;
import com.revature.revshop.repository.WalletTransactionRepository;
import com.revature.revshop.service.TwilioSmsService;
import com.revature.revshop.service.WalletService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final TwilioSmsService twilioSmsService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public WalletServiceImpl(WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            UserRepository userRepository,
            TwilioSmsService twilioSmsService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.userRepository = userRepository;
        this.twilioSmsService = twilioSmsService;
    }

    @Override
    public void sendSmsOtp(Long userId, String mobileNumber) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        twilioSmsService.sendSmsOtp(mobileNumber);
    }

    @Override
    public Wallet verifyMobileKyc(Long userId, String mobileNumber, String otp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isVerified = twilioSmsService.verifyMobileKyc(mobileNumber, otp);
        if (!isVerified) {
            throw new RuntimeException("Invalid OTP provided.");
        }

        Wallet wallet = walletRepository.findByUser_UserId(userId).orElse(new Wallet());
        wallet.setUser(user);
        wallet.setMobileNumber(mobileNumber);
        wallet.setKycVerified(true);
        wallet.setActive(true);

        if (wallet.getBalance() == null) {
            wallet.setBalance(BigDecimal.ZERO);
        }

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUser(Long userId) {
        return walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found or not activated"));
    }

    @Override
    public String createRazorpayOrder(BigDecimal amount) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        // sum must be an integer in paise
        orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "wallet_txn_" + System.currentTimeMillis());

        Order order = razorpay.orders.create(orderRequest);
        return order.get("id");
    }

    @Override
    public Wallet verifyPaymentAndAddMoney(Long userId, BigDecimal amount, String razorpayPaymentId,
            String razorpayOrderId, String razorpaySignature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new RuntimeException("Payment Signature Verification Failed");
            }

            Wallet wallet = getWalletByUser(userId);
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);

            WalletTransaction transaction = new WalletTransaction(
                    wallet,
                    amount,
                    WalletTransaction.TransactionType.CREDIT,
                    "Added funds via Razorpay",
                    razorpayPaymentId);
            walletTransactionRepository.save(transaction);

            return wallet;

        } catch (Exception e) {
            throw new RuntimeException("Error verifying payment: " + e.getMessage());
        }
    }

    @Override
    public boolean deductMoneyFromWallet(Long userId, BigDecimal amount, String description, String referenceId) {
        Wallet wallet = getWalletByUser(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction(
                wallet,
                amount,
                WalletTransaction.TransactionType.DEBIT,
                description,
                referenceId);
        walletTransactionRepository.save(transaction);

        return true;
    }

    @Override
    public List<WalletTransaction> getWalletTransactions(Long userId) {
        Wallet wallet = getWalletByUser(userId);
        return walletTransactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId());
    }
}
