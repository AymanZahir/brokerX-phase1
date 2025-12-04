package com.brokerx.adapters.events;

import com.brokerx.application.events.DomainEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void publish(String topic, String payload) {
    kafkaTemplate
        .send(topic, payload)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.warn("Failed to publish event to topic {}: {}", topic, ex.getMessage());
              } else {
                log.debug("Event published to topic {}", topic);
              }
            });
  }
}
