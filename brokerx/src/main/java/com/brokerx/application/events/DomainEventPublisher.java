package com.brokerx.application.events;

public interface DomainEventPublisher {
  void publish(String topic, String payload);
}
