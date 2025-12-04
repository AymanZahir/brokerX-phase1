package com.brokerx.adapters.web;

import com.brokerx.application.OrderQueryService;
import com.brokerx.application.OrderQueryService.ExecutionView;
import com.brokerx.application.OrderQueryService.NotificationView;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Profile({"monolith", "orders", "test"})
public class OrderQueryController {

  private final OrderQueryService orderQueryService;

  public OrderQueryController(OrderQueryService orderQueryService) {
    this.orderQueryService = orderQueryService;
  }

  @GetMapping("/{orderId}/executions")
  public List<ExecutionDto> executions(@PathVariable UUID orderId) {
    return orderQueryService.executions(orderId).stream()
        .map(
            view ->
                new ExecutionDto(
                    view.executionId(),
                    view.orderId(),
                    view.counterOrderId(),
                    view.qty(),
                    view.price().toPlainString(),
                    view.side()))
        .toList();
  }

  @GetMapping("/{orderId}/notifications")
  public List<NotificationDto> notifications(@PathVariable UUID orderId) {
    return orderQueryService.notifications(orderId).stream()
        .map(
            view ->
                new NotificationDto(
                    view.notificationId(),
                    view.type(),
                    view.channel(),
                    view.status(),
                    view.payload()))
        .toList();
  }

  public record ExecutionDto(
      UUID executionId,
      UUID orderId,
      UUID counterOrderId,
      long qty,
      String price,
      String side) {
    public static ExecutionDto from(ExecutionView view) {
      return new ExecutionDto(
          view.executionId(),
          view.orderId(),
          view.counterOrderId(),
          view.qty(),
          view.price().toPlainString(),
          view.side());
    }
  }

  public record NotificationDto(
      UUID notificationId, String type, String channel, String status, String payload) {
    public static NotificationDto from(NotificationView view) {
      return new NotificationDto(
          view.notificationId(), view.type(), view.channel(), view.status(), view.payload());
    }
  }
}
