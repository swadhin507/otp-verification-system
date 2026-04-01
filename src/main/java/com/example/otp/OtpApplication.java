package com.example.otp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OtpApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtpApplication.class, args);
		System.out.println("=========================================");
		System.out.println("OTP Verification System Started!");
		System.out.println("Access at: http://localhost:8080");
		System.out.println("=========================================");
	}
}