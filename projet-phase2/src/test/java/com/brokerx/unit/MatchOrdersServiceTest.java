package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.entity.ExecutionEntity;
import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.repo.ExecutionJpa;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import com.brokerx.application.AuditLogger;
import com.brokerx.application.MatchOrders;
import com.brokerx.application.NotificationService;
import com.brokerx.application.ObservabilityService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchOrdersServiceTest {

  @Mock private OrderJpa orderJpa;
  @Mock private ExecutionJpa executionJpa;
  @Mock private AuditLogger auditLogger;
  @Mock private NotificationService notificationService;
  @Mock private ObservabilityService observabilityService;

  private MatchOrders matchOrders;
  private Clock clock;

  private UUID buyOrderId;
  private OrderEntity buyOrder;
  private OrderEntity sellOrder;

  @BeforeEach
  void setup() {
    clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    matchOrders =
        new MatchOrders(
            orderJpa, executionJpa, auditLogger, notificationService, observabilityService, clock);

    buyOrderId = UUID.randomUUID();
    buyOrder =
        new OrderEntity(
            buyOrderId,
            UUID.randomUUID(),
            "BUY",
            "LIMIT",
            "AAPL",
            10,
            BigDecimal.valueOf(100),
            "buy-1",
            "WORKING");

    sellOrder =
        new OrderEntity(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "SELL",
            "LIMIT",
            "AAPL",
            6,
            BigDecimal.valueOf(99),
            "sell-1",
            "WORKING");
  }

  @Test
  void matchOrder_consumesBestCandidate() {
    when(orderJpa.findById(buyOrderId)).thenReturn(Optional.of(buyOrder));
    when(orderJpa.findAll()).thenReturn(List.of(buyOrder, sellOrder));

    MatchOrders.MatchResult result = matchOrders.matchOrder(buyOrderId);

    assertEquals("PARTIALLY_FILLED", result.status());
    assertEquals(4, result.remainingQty());
    assertEquals(1, result.executions().size());

    ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderJpa, times(2)).save(orderCaptor.capture());
    OrderEntity savedSell = orderCaptor.getAllValues().get(0);
    assertEquals("FILLED", savedSell.getStatus());
    OrderEntity savedBuy = orderCaptor.getAllValues().get(1);
    assertEquals(4, savedBuy.getQty());

    verify(executionJpa).save(any(ExecutionEntity.class));
    verify(auditLogger)
        .record(
            eq("ORDER"),
            eq("ORDER_MATCHED"),
            eq(buyOrder.getAccountId()),
            any(MatchOrders.MatchAudit.class));
    verify(notificationService, times(2))
        .recordExecution(any(), any(), anyLong(), eq("AAPL"), any(), any());
    verify(observabilityService)
        .recordMatching(any(), eq("AAPL"), eq(6L), eq(1), eq("PARTIALLY_FILLED"));
  }

  @Test
  void matchOrder_returnsNoExecution_whenNoCandidates() {
    when(orderJpa.findById(buyOrderId)).thenReturn(Optional.of(buyOrder));
    when(orderJpa.findAll()).thenReturn(List.of(buyOrder));

    MatchOrders.MatchResult result = matchOrders.matchOrder(buyOrderId);

    assertEquals("WORKING", result.status());
    assertEquals(10, result.remainingQty());
    assertTrue(result.executions().isEmpty());
    verify(orderJpa, never()).save(any(OrderEntity.class));
    verify(executionJpa, never()).save(any());
    verify(auditLogger, never()).record(any(), any(), any(), any());
    verify(notificationService, never()).recordExecution(any(), any(), anyLong(), any(), any(), any());
    verify(observabilityService)
        .recordMatching(any(), eq("AAPL"), eq(0L), eq(0), eq("WORKING"));
  }
}
