package com.brokerx.application.events;

import com.brokerx.adapters.persistence.entity.OutboxEventEntity;
import com.brokerx.adapters.persistence.repo.OutboxEventJpa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DomainEventBus implements DomainEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(DomainEventBus.class);

  private final Map<String, List<DomainEventHandler<?>>> handlers = new ConcurrentHashMap<>();
  private final OutboxEventJpa outboxEventJpa;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public DomainEventBus(OutboxEventJpa outboxEventJpa, ObjectMapper objectMapper, Clock clock) {
    this.outboxEventJpa = outboxEventJpa;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  public void register(DomainEventHandler<?> handler) {
    handlers.computeIfAbsent(handler.eventType(), ignored -> new CopyOnWriteArrayList<>()).add(handler);
    log.info("Registered event handler for type={}", handler.eventType());
  }

  @Override
  @Transactional
  public List<Object> publish(String eventType, UUID aggregateId, Object payload) {
    OutboxEventEntity stored =
        new OutboxEventEntity(
            UUID.randomUUID(),
            eventType,
            aggregateId,
            serializePayload(payload),
            "PENDING",
            Instant.now(clock));
    outboxEventJpa.save(stored);
    return dispatch(stored, payload);
  }

  public List<Object> replayPending() {
    List<OutboxEventEntity> events = outboxEventJpa.findByStatus("PENDING");
    List<Object> allResults = new ArrayList<>();
    for (OutboxEventEntity event : events) {
      allResults.addAll(dispatch(event, null));
    }
    return allResults;
  }

  private List<Object> dispatch(OutboxEventEntity stored, Object payloadOverride) {
    List<Object> results = new ArrayList<>();
    try {
      for (DomainEventHandler<?> handler : handlers.getOrDefault(stored.getType(), List.of())) {
        Object convertedPayload = convertPayload(stored, payloadOverride, handler.payloadType());
        @SuppressWarnings("unchecked")
        DomainEventHandler<Object> typedHandler = (DomainEventHandler<Object>) handler;
        Object result =
            typedHandler.handle(
                new EventEnvelope<>(
                    stored.getId(), stored.getType(), stored.getAggregateId(), convertedPayload, stored.getCreatedAt()));
        if (result != null) {
          results.add(result);
        }
      }
      stored.setStatus("PROCESSED");
      stored.setProcessedAt(Instant.now(clock));
    } catch (Exception e) {
      stored.setStatus("FAILED");
      stored.setErrorMessage(e.getMessage());
      log.error("Dispatch of event {} failed: {}", stored.getType(), e.getMessage(), e);
      throw e;
    } finally {
      outboxEventJpa.save(stored);
    }
    return results;
  }

  private String serializePayload(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize event payload", e);
    }
  }

  private Object convertPayload(OutboxEventEntity stored, Object providedPayload, Class<?> targetType) {
    Object payload = providedPayload != null ? providedPayload : stored.getPayload();
    if (providedPayload != null && targetType.isInstance(providedPayload)) {
      return targetType.cast(providedPayload);
    }
    try {
      if (payload instanceof String json) {
        return objectMapper.readValue(json, targetType);
      }
      return objectMapper.convertValue(payload, targetType);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Cannot convert payload for event %s to %s".formatted(stored.getType(), targetType.getSimpleName()), e);
    }
  }
}
