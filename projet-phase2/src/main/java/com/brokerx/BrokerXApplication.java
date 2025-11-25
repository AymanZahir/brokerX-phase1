package com.brokerx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrokerXApplication {

  public static void main(String[] args) {
    SpringApplication.run(BrokerXApplication.class, args);
  }
}
