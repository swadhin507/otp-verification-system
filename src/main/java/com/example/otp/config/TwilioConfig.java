package com.example.otp.config;

import com.twilio.Twilio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String phoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() &&
                !accountSid.equals("YOUR_TWILIO_ACCOUNT_SID")) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully with phone number: {}", phoneNumber);
        } else {
            log.warn("Twilio not configured. OTP will be printed to console only.");
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}