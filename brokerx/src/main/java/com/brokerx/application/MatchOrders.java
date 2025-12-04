package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.ExecutionEntity;
import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.repo.ExecutionJpa;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchOrders {

  private final OrderJpa orders;
  private final ExecutionJpa executions;
  private final AuditLogger auditLogger;
  private final NotificationService notificationService;
  private final ObservabilityService observabilityService;
  private final Clock clock;

  public MatchOrders(
      OrderJpa orders,
      ExecutionJpa executions,
      AuditLogger auditLogger,
      NotificationService notificationService,
      ObservabilityService observabilityService,
      Clock clock) {
    this.orders = orders;
    this.executions = executions;
    this.auditLogger = auditLogger;
    this.notificationService = notificationService;
    this.observabilityService = observabilityService;
    this.clock = clock;
  }

  public record MatchResult(UUID orderId, String status, long remainingQty, List<ExecutionEvent> executions) {}

  public record ExecutionEvent(UUID executionId, UUID orderId, UUID counterOrderId, long qty, BigDecimal price) {}

  @Transactional
  public MatchResult matchOrder(UUID orderId) {
    OrderEntity incoming =
        orders
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

    if (!isOpenStatus(incoming.getStatus())) {
      return new MatchResult(incoming.getId(), incoming.getStatus(), incoming.getQty(), List.of());
    }

    List<OrderEntity> candidates = findCandidates(incoming);

    long originalQty = incoming.getQty();
    long qtyRemaining = originalQty;
    List<ExecutionEvent> produced = new ArrayList<>();
    Timer.Sample sample = observabilityService.startMatchingSample();

    for (OrderEntity candidate : candidates) {
      if (qtyRemaining <= 0) {
        break;
      }

      long matchQty = Math.min(qtyRemaining, candidate.getQty());
      BigDecimal executionPrice = determinePrice(incoming, candidate);
      Instant now = clock.instant();

      candidate.setQty(candidate.getQty() - matchQty);
      if (candidate.getQty() == 0) {
        candidate.setStatus("FILLED");
      } else {
        candidate.setStatus("PARTIALLY_FILLED");
      }
      candidate.setVersion(candidate.getVersion() + 1);
      orders.save(candidate);

      qtyRemaining -= matchQty;

      ExecutionEntity execution =
          new ExecutionEntity(
              UUID.randomUUID(),
              incoming.getId(),
              candidate.getId(),
              incoming.getAccountId(),
              candidate.getAccountId(),
              incoming.getSymbol(),
              matchQty,
              executionPrice,
              incoming.getSide().toUpperCase(Locale.ROOT));
      execution.setExecutionTime(now);
      executions.save(execution);

      notificationService.recordExecution(
          incoming.getAccountId(),
          incoming.getId(),
          matchQty,
          incoming.getSymbol(),
          incoming.getSide(),
          executionPrice.toPlainString());
      notificationService.recordExecution(
          candidate.getAccountId(),
          candidate.getId(),
          matchQty,
          incoming.getSymbol(),
          candidate.getSide(),
          executionPrice.toPlainString());

      produced.add(new ExecutionEvent(execution.getId(), incoming.getId(), candidate.getId(), matchQty, executionPrice));
    }

    if (!produced.isEmpty()) {
      if (qtyRemaining == 0) {
        incoming.setStatus("FILLED");
      } else if (qtyRemaining < originalQty) {
        incoming.setStatus("PARTIALLY_FILLED");
        incoming.setQty(qtyRemaining);
      }
      incoming.setVersion(incoming.getVersion() + 1);
      orders.save(incoming);

      auditLogger.record(
          "ORDER",
          "ORDER_MATCHED",
          incoming.getAccountId(),
          new MatchAudit(incoming.getId(), incoming.getStatus(), qtyRemaining, produced.size()));
    }

    long matchedQty = originalQty - qtyRemaining;
    observabilityService.recordMatching(sample, incoming.getSymbol(), matchedQty, produced.size(), incoming.getStatus());

    return new MatchResult(incoming.getId(), incoming.getStatus(), qtyRemaining, produced);
  }

  private List<OrderEntity> findCandidates(OrderEntity incoming) {
    String requiredSide = "BUY".equalsIgnoreCase(incoming.getSide()) ? "SELL" : "BUY";

    Comparator<OrderEntity> comparator;
    if ("BUY".equalsIgnoreCase(incoming.getSide())) {
      comparator =
          Comparator.comparing(
                  (OrderEntity o) ->
                      o.getLimitPrice() == null
                          ? BigDecimal.valueOf(Double.MAX_VALUE)
                          : o.getLimitPrice())
              .thenComparing(OrderEntity::getCreatedAt);
    } else {
      comparator =
          Comparator.comparing(
                  (OrderEntity o) ->
                      o.getLimitPrice() == null ? BigDecimal.ZERO : o.getLimitPrice())
              .reversed()
              .thenComparing(OrderEntity::getCreatedAt);
    }

    return orders.findAll().stream()
        .filter(o -> !o.getId().equals(incoming.getId()))
        .filter(o -> isOpenStatus(o.getStatus()))
        .filter(o -> o.getSymbol().equalsIgnoreCase(incoming.getSymbol()))
        .filter(o -> o.getSide().equalsIgnoreCase(requiredSide))
        .filter(o -> priceSatisfies(incoming, o))
        .sorted(comparator)
        .toList();
  }

  private boolean priceSatisfies(OrderEntity incoming, OrderEntity candidate) {
    if ("MARKET".equalsIgnoreCase(incoming.getType()) || "MARKET".equalsIgnoreCase(candidate.getType())) {
      return true;
    }
    if ("BUY".equalsIgnoreCase(incoming.getSide())) {
      if (incoming.getLimitPrice() == null || candidate.getLimitPrice() == null) {
        return true;
      }
      return candidate.getLimitPrice().compareTo(incoming.getLimitPrice()) <= 0;
    }
    if (incoming.getLimitPrice() == null || candidate.getLimitPrice() == null) {
      return true;
    }
    return candidate.getLimitPrice().compareTo(incoming.getLimitPrice()) >= 0;
  }

  private boolean isOpenStatus(String status) {
    return !"CANCELED".equalsIgnoreCase(status) && !"FILLED".equalsIgnoreCase(status);
  }

  private BigDecimal determinePrice(OrderEntity incoming, OrderEntity candidate) {
    if ("MARKET".equalsIgnoreCase(incoming.getType())) {
      if (candidate.getLimitPrice() != null) {
    return candidate.getLimitPrice();
  }
      return incoming.getLimitPrice() != null ? incoming.getLimitPrice() : BigDecimal.valueOf(100);
    }
    if ("MARKET".equalsIgnoreCase(candidate.getType())) {
      return incoming.getLimitPrice() != null ? incoming.getLimitPrice() : BigDecimal.valueOf(100);
    }
    if ("BUY".equalsIgnoreCase(incoming.getSide())) {
      return candidate.getLimitPrice();
    }
    return incoming.getLimitPrice();
  }

  public record MatchAudit(UUID orderId, String status, long remainingQty, int executions) {}
}
