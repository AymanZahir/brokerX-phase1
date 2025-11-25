package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import com.brokerx.application.events.DomainEventPublisher;
import com.brokerx.application.events.OrderEvents;
import com.brokerx.application.events.OrderMatchedEvent;
import com.brokerx.application.events.OrderPlacedEvent;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaceOrder {

  private final OrderJpa orders;
  private final WalletJpa wallets;
  private final AuditLogger auditLogger;
  private final ObservabilityService observabilityService;
  private final DomainEventPublisher eventPublisher;

  public PlaceOrder(
      OrderJpa orders,
      WalletJpa wallets,
      AuditLogger auditLogger,
      ObservabilityService observabilityService,
      DomainEventPublisher eventPublisher) {
    this.orders = orders;
    this.wallets = wallets;
    this.auditLogger = auditLogger;
    this.observabilityService = observabilityService;
    this.eventPublisher = eventPublisher;
  }

  public record Draft(
      UUID accountId,
      String side,
      String type,
      String symbol,
      long qty,
      BigDecimal limitPrice,
      String clientOrderId) {}

  public record Ack(UUID orderId, String status) {}

  @Transactional
  public Ack handle(Draft draft) {
    Optional<OrderEntity> prior = orders.findByClientOrderId(draft.clientOrderId());
    if (prior.isPresent()) {
      OrderEntity existing = prior.get();
      auditLogger.record(
          "ORDER",
          "ORDER_IDEMPOTENT",
          existing.getAccountId(),
          new OrderAudit(existing.getId(), existing.getStatus(), draft.clientOrderId()));
      return new Ack(existing.getId(), existing.getStatus());
    }

    if (draft.qty() <= 0) {
      throw new IllegalArgumentException("qty>0");
    }
    if ("LIMIT".equals(draft.type())
        && (draft.limitPrice() == null || draft.limitPrice().signum() <= 0)) {
      throw new IllegalArgumentException("limitPrice>0 for LIMIT");
    }

    wallets.findByAccountId(draft.accountId()).orElseThrow();

    OrderEntity order =
        new OrderEntity(
            UUID.randomUUID(),
            draft.accountId(),
            draft.side(),
            draft.type(),
            draft.symbol(),
            draft.qty(),
            draft.limitPrice(),
            draft.clientOrderId(),
            "WORKING");
    orders.save(order);

    observabilityService.recordOrderAccepted(order.getSymbol(), order.getType());

    OrderPlacedEvent placedEvent =
        new OrderPlacedEvent(
            order.getId(),
            order.getAccountId(),
            order.getSide(),
            order.getType(),
            order.getSymbol(),
            order.getQty(),
            order.getLimitPrice());

    String status = order.getStatus();
    for (Object response :
        eventPublisher.publish(OrderEvents.ORDER_PLACED, order.getId(), placedEvent)) {
      if (response instanceof OrderMatchedEvent matchedEvent) {
        status = matchedEvent.status();
        break;
      }
    }
    Ack ack = new Ack(order.getId(), status);
    auditLogger.record(
        "ORDER",
        "ORDER_ACCEPTED",
        order.getAccountId(),
        new OrderAudit(order.getId(), order.getStatus(), draft.clientOrderId()));
    return ack;
  }

  public record OrderAudit(UUID orderId, String status, String clientOrderId) {}
}
