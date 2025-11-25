package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import com.brokerx.application.AuditLogger;
import com.brokerx.application.ModifyOrder;
import com.brokerx.application.support.OrderCacheService;
import java.math.BigDecimal;
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
class ModifyOrderServiceTest {

  @Mock private OrderJpa orderJpa;
  @Mock private AuditLogger auditLogger;
  @Mock private OrderCacheService orderCacheService;

  private ModifyOrder modifyOrder;
  private Clock clock;

  private UUID orderId;
  private OrderEntity existing;

  @BeforeEach
  void setup() {
    clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    modifyOrder = new ModifyOrder(orderJpa, auditLogger, clock, orderCacheService);

    orderId = UUID.randomUUID();
    existing =
        new OrderEntity(
            orderId,
            UUID.randomUUID(),
            "BUY",
            "LIMIT",
            "AAPL",
            10,
            BigDecimal.valueOf(100),
            "client-1",
            "WORKING");
    existing.setVersion(2);
  }

  @Test
  void replace_updatesQtyAndLimit_whenValid() {
    when(orderJpa.findById(orderId)).thenReturn(Optional.of(existing));

    ModifyOrder.OrderMutation mutation =
        modifyOrder.replace(
            new ModifyOrder.ReplaceCommand(
                existing.getAccountId(), orderId, 2, 15L, BigDecimal.valueOf(101), "DAY"));

    assertEquals("WORKING", mutation.status());
    assertEquals(3, mutation.version());
    ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderJpa).save(orderCaptor.capture());
    OrderEntity saved = orderCaptor.getValue();
    assertEquals(15, saved.getQty());
    assertEquals(BigDecimal.valueOf(101), saved.getLimitPrice());
    assertEquals(3, saved.getVersion());
    verify(auditLogger)
        .record(
            eq("ORDER"),
            eq("ORDER_REPLACED"),
            eq(existing.getAccountId()),
            any(ModifyOrder.OrderReplaceAudit.class));
    verify(orderCacheService).evictForOrder(orderId);
  }

  @Test
  void cancel_setsStatusCanceled_whenVersionMatches() {
    when(orderJpa.findById(orderId)).thenReturn(Optional.of(existing));

    ModifyOrder.OrderMutation mutation =
        modifyOrder.cancel(new ModifyOrder.CancelCommand(existing.getAccountId(), orderId, 2));

    assertEquals("CANCELED", mutation.status());
    ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderJpa).save(orderCaptor.capture());
    assertEquals("CANCELED", orderCaptor.getValue().getStatus());
    assertEquals(3, orderCaptor.getValue().getVersion());
    verify(auditLogger)
        .record(
            eq("ORDER"),
            eq("ORDER_CANCELED"),
            eq(existing.getAccountId()),
            any(ModifyOrder.OrderCancelAudit.class));
    verify(orderCacheService, atLeastOnce()).evictForOrder(orderId);
  }

  @Test
  void replace_throwsWhenAccountMismatch() {
    when(orderJpa.findById(orderId)).thenReturn(Optional.of(existing));
    UUID otherAccount = UUID.randomUUID();
    assertThrows(
        IllegalStateException.class,
        () ->
            modifyOrder.replace(
                new ModifyOrder.ReplaceCommand(
                    otherAccount, orderId, 2, 15L, BigDecimal.valueOf(101), "DAY")));
  }

  @Test
  void replace_throwsWhenVersionMismatch() {
    when(orderJpa.findById(orderId)).thenReturn(Optional.of(existing));
    assertThrows(
        IllegalStateException.class,
        () ->
            modifyOrder.replace(
                new ModifyOrder.ReplaceCommand(
                    existing.getAccountId(), orderId, 1, 20L, null, null)));
  }
}
