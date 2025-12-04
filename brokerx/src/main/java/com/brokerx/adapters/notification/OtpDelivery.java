package com.brokerx.adapters.notification;

public interface OtpDelivery {
  void send(String email, String otp, String verificationId);
}
