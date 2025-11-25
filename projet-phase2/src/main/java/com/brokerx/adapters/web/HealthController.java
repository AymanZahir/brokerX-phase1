package com.brokerx.adapters.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping({"/health", "/actuator/health"})
  public Map<String, Object> health() {
    return Map.of("status", "UP", "timestamp", Instant.now().toString());
  }
}
