package com.example.otp.controller;

import com.example.otp.model.ApiResponse;
import com.example.otp.model.OtpRequest;
import com.example.otp.model.OtpVerificationRequest;
import com.example.otp.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "*")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        try {
            log.info("Sending OTP to mobile number: {}", request.getMobileNumber());
            String otp = otpService.generateAndSendOtp(request.getMobileNumber());
            return ResponseEntity.ok(new ApiResponse(true,
                    "OTP sent successfully to " + request.getMobileNumber(), null));
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            log.info("Verifying OTP for mobile number: {}", request.getMobileNumber());
            boolean isValid = otpService.verifyOtp(request.getMobileNumber(), request.getOtp());
            if (isValid) {
                return ResponseEntity.ok(new ApiResponse(true,
                        "OTP verified successfully!", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Invalid OTP", null));
            }
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(new ApiResponse(true, "OTP Service is running", null));
    }
}