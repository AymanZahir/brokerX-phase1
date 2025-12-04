package com.brokerx.adapters.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "brokerx.mail")
public class BrokerxMailProperties {

  private boolean enabled;
  private String from;
  private String toOverride;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getToOverride() {
    return toOverride;
  }

  public void setToOverride(String toOverride) {
    this.toOverride = toOverride;
  }

  public record View(boolean enabled, String from, String toOverride) {}

  public View view() {
    return new View(enabled, from, toOverride);
  }
}
