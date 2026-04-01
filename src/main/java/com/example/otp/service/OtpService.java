package com.example.otp.service;

import com.example.otp.model.OtpData;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OtpService {

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.expiry.minutes:5}")
    private int expiryMinutes;

    @Value("${otp.max.attempts:5}")
    private int maxAttempts;

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    @Value("${otp.redis.key.prefix:otp:}")
    private String redisKeyPrefix;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
                !twilioAccountSid.equals("YOUR_TWILIO_ACCOUNT_SID")) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio not configured. OTP will be printed to console only.");
        }
    }

    public String generateAndSendOtp(String mobileNumber) {
        String redisKey = redisKeyPrefix + mobileNumber;

        // Check existing OTP and attempts
        OtpData existingOtp = (OtpData) redisTemplate.opsForValue().get(redisKey);
        if (existingOtp != null && existingOtp.getAttempts() >= maxAttempts) {
            throw new RuntimeException("Maximum attempts exceeded. Please request a new OTP after 5 minutes.");
        }

        // Generate new OTP
        String otp = generateOtp();
        long expiryTime = System.currentTimeMillis() + (expiryMinutes * 60 * 1000);

        OtpData otpData = new OtpData(otp, expiryTime, 0);

        // Store in Redis with expiry (expiryMinutes + 1 minute buffer)
        redisTemplate.opsForValue().set(redisKey, otpData, expiryMinutes + 1, TimeUnit.MINUTES);

        // Send OTP
        sendOtpViaSms(mobileNumber, otp);

        log.info("OTP generated for {}: {}", mobileNumber, otp);
        return otp;
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        String redisKey = redisKeyPrefix + mobileNumber;
        OtpData otpData = (OtpData) redisTemplate.opsForValue().get(redisKey);

        if (otpData == null) {
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }

        // Check expiry
        if (System.currentTimeMillis() > otpData.getExpiryTime()) {
            redisTemplate.delete(redisKey);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        // Check attempts
        if (otpData.getAttempts() >= maxAttempts) {
            redisTemplate.delete(redisKey);
            throw new RuntimeException("Maximum attempts exceeded. Please request a new OTP.");
        }

        // Increment attempts
        otpData.setAttempts(otpData.getAttempts() + 1);
        redisTemplate.opsForValue().set(redisKey, otpData,
                getRemainingTime(otpData.getExpiryTime()), TimeUnit.MILLISECONDS);

        // Verify OTP
        if (otpData.getOtp().equals(otp)) {
            redisTemplate.delete(redisKey);
            log.info("OTP verified successfully for {}", mobileNumber);
            return true;
        }

        int remainingAttempts = maxAttempts - otpData.getAttempts();
        throw new RuntimeException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private void sendOtpViaSms(String mobileNumber, String otp) {
        String message = "Your OTP for verification is: " + otp +
                ". This OTP is valid for " + expiryMinutes + " minutes. " +
                "Do not share this OTP with anyone.";

        // Check if Twilio is configured for production
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
                !twilioAccountSid.equals("YOUR_TWILIO_ACCOUNT_SID")) {
            try {
                Message.creator(
                        new PhoneNumber("+91" + mobileNumber),
                        new PhoneNumber(twilioPhoneNumber),
                        message
                ).create();
                log.info("SMS sent successfully to {}", mobileNumber);
            } catch (Exception e) {
                log.error("Failed to send SMS: {}", e.getMessage());
                printOtpToConsole(mobileNumber, otp);
            }
        } else {
            // Console output for testing
            printOtpToConsole(mobileNumber, otp);
        }
    }

    private void printOtpToConsole(String mobileNumber, String otp) {
        System.out.println("\n=========================================");
        System.out.println("📱 Mobile Number: " + mobileNumber);
        System.out.println("🔐 OTP: " + otp);
        System.out.println("⏱️  Valid for: " + expiryMinutes + " minutes");
        System.out.println("=========================================\n");
    }

    private long getRemainingTime(long expiryTime) {
        long remaining = expiryTime - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public void cleanupExpiredOtps() {
        // Redis handles expiry automatically with TTL
        log.debug("Redis automatically handles OTP cleanup with TTL");
    }
}