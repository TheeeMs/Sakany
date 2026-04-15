package com.theMs.sakany.shared.firebase;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

  private static final int OTP_LENGTH = 6;
  private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes
  private final SecureRandom random = new SecureRandom();

  // In-memory storage (use Redis in production)
  private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

  public String generateOtp(String phoneNumber) {
    String otp = String.format("%06d", random.nextInt(1000000));
    otpStore.put(phoneNumber, new OtpData(otp, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS)));

    // TODO: Send OTP via SMS (Firebase Phone Auth or SMS service)
    System.out.println("OTP for " + phoneNumber + ": " + otp);

    return otp;
  }

  public boolean verifyOtp(String phoneNumber, String otp) {
    OtpData otpData = otpStore.get(phoneNumber);

    if (otpData == null) {
      return false;
    }

    if (Instant.now().isAfter(otpData.expiresAt)) {
      otpStore.remove(phoneNumber);
      return false;
    }

    boolean isValid = otpData.code.equals(otp);
    if (isValid) {
      otpStore.remove(phoneNumber); // One-time use
    }

    return isValid;
  }

  private record OtpData(String code, Instant expiresAt) {
  }
}
