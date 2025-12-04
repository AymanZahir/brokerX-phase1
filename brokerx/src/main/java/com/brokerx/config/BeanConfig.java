package com.brokerx.config;

import com.brokerx.adapters.notification.NoopOtpDelivery;
import com.brokerx.adapters.notification.OtpDelivery;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Bean
  public Clock systemClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnMissingBean(OtpDelivery.class)
  public OtpDelivery otpDeliveryFallback() {
    return new NoopOtpDelivery();
  }
}
