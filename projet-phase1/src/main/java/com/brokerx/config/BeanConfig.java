package com.brokerx.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Bean
  public Clock systemClock() {
    return Clock.systemUTC();
  }
}
