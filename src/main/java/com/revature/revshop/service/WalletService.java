package com.revature.revshop.service;

import com.revature.revshop.model.Wallet;
import com.revature.revshop.model.WalletTransaction;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    void sendSmsOtp(Long userId, String mobileNumber);

    Wallet verifyMobileKyc(Long userId, String mobileNumber, String otp);

    Wallet getWalletByUser(Long userId);

    String createRazorpayOrder(BigDecimal amount) throws Exception;

    Wallet verifyPaymentAndAddMoney(Long userId, BigDecimal amount, String razorpayPaymentId, String razorpayOrderId,
            String razorpaySignature);

    boolean deductMoneyFromWallet(Long userId, BigDecimal amount, String description, String referenceId);

    List<WalletTransaction> getWalletTransactions(Long userId);
}
