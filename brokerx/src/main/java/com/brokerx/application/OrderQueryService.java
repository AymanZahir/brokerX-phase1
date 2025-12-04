package com.brokerx.application;

import com.brokerx.adapters.persistence.repo.ExecutionJpa;
import com.brokerx.adapters.persistence.repo.NotificationJpa;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {

  private final ExecutionJpa executions;
  private final NotificationJpa notifications;

  public OrderQueryService(ExecutionJpa executions, NotificationJpa notifications) {
    this.executions = executions;
    this.notifications = notifications;
  }

  @Cacheable(cacheNames = "orderExecutions", key = "#orderId")
  public List<ExecutionView> executions(UUID orderId) {
    return executions.findByOrderId(orderId).stream()
        .map(
            e ->
                new ExecutionView(
                    e.getId(),
                    e.getOrderId(),
                    e.getCounterOrderId(),
                    e.getQty(),
                    e.getPrice(),
                    e.getSide()))
        .toList();
  }

  @Cacheable(cacheNames = "orderNotifications", key = "#orderId")
  public List<NotificationView> notifications(UUID orderId) {
    return notifications.findByOrderId(orderId).stream()
        .map(
            n ->
                new NotificationView(
                    n.getId(), n.getType(), n.getChannel(), n.getStatus(), n.getPayload()))
        .toList();
  }

  public record ExecutionView(
      UUID executionId,
      UUID orderId,
      UUID counterOrderId,
      long qty,
      BigDecimal price,
      String side) {}

  public record NotificationView(
      UUID notificationId, String type, String channel, String status, String payload) {}
}
