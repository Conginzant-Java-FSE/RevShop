package com.revature.revshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    // Cache to store OTPs: <Email, OtpData>
    private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final EmailService emailService;

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    // Generate a 6-digit OTP
    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        // Valid for 10 minutes
        otpCache.put(email.toLowerCase(), new OtpData(otp, LocalDateTime.now().plusMinutes(10)));
        log.info("OTP generated for {}", email);
        log.info("Sending OTP email to {}", email);
        emailService.sendOtpEmail(email, otp);
        return otp;
    }

    // Verify the OTP
    public boolean verifyOtp(String email, String enteredOtp) {
        String key = email.toLowerCase();
        OtpData otpData = otpCache.get(key);

        if (otpData == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.expiryDate)) {
            otpCache.remove(key); // clear expired
            return false;
        }

        if (otpData.otp.equals(enteredOtp)) {
            otpCache.remove(key); // clear after successful verification
            return true;
        }

        return false;
    }

    private static class OtpData {
        String otp;
        LocalDateTime expiryDate;

        OtpData(String otp, LocalDateTime expiryDate) {
            this.otp = otp;
            this.expiryDate = expiryDate;
        }
    }
}
