package com.brokerx.adapters.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(OtpDelivery.class)
public class NoopOtpDelivery implements OtpDelivery {

  private static final Logger log = LoggerFactory.getLogger(NoopOtpDelivery.class);

  @Override
  public void send(String email, String otp, String verificationId) {
    log.info("OTP email disabled. Pretending to send OTP={} to {}", otp, email);
  }
}
