package com.example.otp.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String otp;
    private long expiryTime;
    private int attempts;
}