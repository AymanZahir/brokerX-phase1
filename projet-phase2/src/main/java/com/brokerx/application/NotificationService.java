package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.NotificationEntity;
import com.brokerx.adapters.persistence.repo.NotificationJpa;
import com.brokerx.application.support.OrderCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationJpa notifications;
  private final ObjectMapper objectMapper;
  private final ObservabilityService observabilityService;
  private final Clock clock;
  private final OrderCacheService orderCacheService;

  public NotificationService(
      NotificationJpa notifications,
      ObjectMapper objectMapper,
      ObservabilityService observabilityService,
      Clock clock,
      OrderCacheService orderCacheService) {
    this.notifications = notifications;
    this.objectMapper = objectMapper;
    this.observabilityService = observabilityService;
    this.clock = clock;
    this.orderCacheService = orderCacheService;
  }

  @Transactional
  public UUID recordExecution(UUID accountId, UUID orderId, long qty, String symbol, String side, String price) {
    String payload =
        serialize(
            Map.of(
                "orderId", orderId,
                "qty", qty,
                "symbol", symbol,
                "side", side,
                "price", price));

    NotificationEntity entity =
        new NotificationEntity(UUID.randomUUID(), accountId, orderId, "ORDER_EXECUTED", "IN_APP", payload);
    entity.setCreatedAt(clock.instant());
    notifications.save(entity);
    observabilityService.recordNotification("ORDER_EXECUTED", "IN_APP");
    orderCacheService.evictForOrder(orderId);
    return entity.getId();
  }

  private String serialize(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize notification payload", e);
      return "{}";
    }
  }

  public record CanadaPrice(String currency, String amount) {
    public String value() {
      return amount;
    }
  }
}
