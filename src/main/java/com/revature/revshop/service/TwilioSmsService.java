package com.revature.revshop.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.verify.service.sid}")
    private String verifyServiceSid;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSmsOtp(String mobileNumber) {
        // Automatically append +91 for Indian numbers if they forgot
        if (!mobileNumber.startsWith("+")) {
            mobileNumber = "+91" + mobileNumber;
        }

        Verification.creator(
                verifyServiceSid,
                mobileNumber,
                "sms").create();
    }

    public boolean verifyMobileKyc(String mobileNumber, String otp) {
        if (!mobileNumber.startsWith("+")) {
            mobileNumber = "+91" + mobileNumber;
        }

        VerificationCheck verificationCheck = VerificationCheck.creator(
                verifyServiceSid)
                .setTo(mobileNumber)
                .setCode(otp)
                .create();

        return "approved".equalsIgnoreCase(verificationCheck.getStatus());
    }
}
