package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.brokerx.adapters.persistence.entity.NotificationEntity;
import com.brokerx.adapters.persistence.repo.NotificationJpa;
import com.brokerx.application.NotificationService;
import com.brokerx.application.ObservabilityService;
import com.brokerx.application.support.OrderCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationJpa notificationJpa;
  @Mock private ObservabilityService observabilityService;
  @Mock private OrderCacheService orderCacheService;

  private NotificationService notificationService;
  private Clock clock;

  @BeforeEach
  void setup() {
    clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    notificationService =
        new NotificationService(
            notificationJpa, new ObjectMapper(), observabilityService, clock, orderCacheService);
  }

  @Test
  void recordExecution_savesNotification() {
    UUID accountId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    UUID notificationId = notificationService.recordExecution(accountId, orderId, 5, "AAPL", "BUY", "101.50");

    assertNotNull(notificationId);
    ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
    verify(notificationJpa).save(captor.capture());
    NotificationEntity saved = captor.getValue();
    assertEquals(accountId, saved.getAccountId());
    assertEquals("ORDER_EXECUTED", saved.getType());
    verify(observabilityService).recordNotification("ORDER_EXECUTED", "IN_APP");
    verify(orderCacheService).evictForOrder(orderId);
  }
}
