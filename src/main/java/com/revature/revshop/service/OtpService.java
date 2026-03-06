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
    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static class OtpDetails {
        String code;
        LocalDateTime expiry;

        OtpDetails(String code) {
            this.code = code;
            this.expiry = LocalDateTime.now().plusMinutes(5); // OTP valid for 5 mins
        }
    }

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        otpStorage.put(email, new OtpDetails(otp));
        log.info("OTP generated for email={}", email);
        return otp;
    }

    public boolean verifyOtp(String email, String code) {
        OtpDetails details = otpStorage.get(email);
        if (details == null) {
            return false;
        }

        if (details.expiry.isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            return false;
        }

        boolean isValid = details.code.equals(code);
        if (isValid) {
            otpStorage.remove(email); // One-time use
        }
        return isValid;
    }
}
