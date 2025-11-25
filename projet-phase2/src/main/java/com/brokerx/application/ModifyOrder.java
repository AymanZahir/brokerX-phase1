package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import com.brokerx.application.support.OrderCacheService;
import com.brokerx.application.events.DomainEventPublisher;
import com.brokerx.application.events.OrderEvents;
import com.brokerx.application.events.OrderUpdatedEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModifyOrder {

  private final OrderJpa orders;
  private final AuditLogger auditLogger;
  private final Clock clock;
  private final OrderCacheService orderCacheService;
  private final DomainEventPublisher eventPublisher;

  public ModifyOrder(
      OrderJpa orders,
      AuditLogger auditLogger,
      Clock clock,
      OrderCacheService orderCacheService,
      DomainEventPublisher eventPublisher) {
    this.orders = orders;
    this.auditLogger = auditLogger;
    this.clock = clock;
    this.orderCacheService = orderCacheService;
    this.eventPublisher = eventPublisher;
  }

  public record ReplaceCommand(
      UUID accountId,
      UUID orderId,
      int expectedVersion,
      Long newQty,
      BigDecimal newLimitPrice,
      String duration) {}

  public record CancelCommand(UUID accountId, UUID orderId, int expectedVersion) {}

  public record OrderMutation(UUID orderId, String status, int version, Instant timestamp) {}

  @Transactional
  public OrderMutation replace(ReplaceCommand cmd) {
    OrderEntity order = orders.findById(cmd.orderId()).orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));
    ensureOwnership(cmd.accountId(), order);

    ensureMutable(cmd.expectedVersion(), order);

    if (cmd.newQty != null) {
      if (cmd.newQty <= 0) {
        throw new IllegalArgumentException("qty>0");
      }
      order.setQty(cmd.newQty);
    }
    if ("LIMIT".equals(order.getType()) && cmd.newLimitPrice != null) {
      if (cmd.newLimitPrice.signum() <= 0) {
        throw new IllegalArgumentException("limitPrice>0");
      }
      order.setLimitPrice(cmd.newLimitPrice);
    }

    order.setVersion(order.getVersion() + 1);
    orders.save(order);

    OrderMutation mutation =
        new OrderMutation(order.getId(), order.getStatus(), order.getVersion(), clock.instant());
    auditLogger.record(
        "ORDER",
        "ORDER_REPLACED",
        order.getAccountId(),
        new OrderReplaceAudit(order.getId(), order.getVersion(), cmd.newQty, cmd.newLimitPrice));
    orderCacheService.evictForOrder(order.getId());
    eventPublisher.publish(
        OrderEvents.ORDER_UPDATED,
        order.getId(),
        new OrderUpdatedEvent(
            order.getId(),
            order.getAccountId(),
            order.getStatus(),
            order.getVersion(),
            order.getQty(),
            order.getLimitPrice()));
    return mutation;
  }

  @Transactional
  public OrderMutation cancel(CancelCommand cmd) {
    OrderEntity order = orders.findById(cmd.orderId()).orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

    ensureOwnership(cmd.accountId(), order);

    ensureMutable(cmd.expectedVersion(), order);

    order.setStatus("CANCELED");
    order.setVersion(order.getVersion() + 1);
    orders.save(order);

    OrderMutation mutation =
        new OrderMutation(order.getId(), order.getStatus(), order.getVersion(), clock.instant());
    auditLogger.record(
        "ORDER",
        "ORDER_CANCELED",
        order.getAccountId(),
        new OrderCancelAudit(order.getId(), order.getVersion()));
    orderCacheService.evictForOrder(order.getId());
    eventPublisher.publish(
        OrderEvents.ORDER_CANCELED,
        order.getId(),
        new OrderUpdatedEvent(
            order.getId(),
            order.getAccountId(),
            order.getStatus(),
            order.getVersion(),
            order.getQty(),
            order.getLimitPrice()));
    return mutation;
  }

  private void ensureMutable(int expectedVersion, OrderEntity order) {
    if (order.getVersion() != expectedVersion) {
      throw new IllegalStateException("ORDER_VERSION_CONFLICT");
    }
    if (!"WORKING".equals(order.getStatus())) {
      throw new IllegalStateException("ORDER_NOT_MODIFIABLE");
    }
  }

  private void ensureOwnership(UUID accountId, OrderEntity order) {
    if (!order.getAccountId().equals(accountId)) {
      throw new IllegalStateException("ORDER_ACCOUNT_MISMATCH");
    }
  }

  public record OrderReplaceAudit(UUID orderId, int version, Long qty, BigDecimal limitPrice) {}

  public record OrderCancelAudit(UUID orderId, int version) {}
}
