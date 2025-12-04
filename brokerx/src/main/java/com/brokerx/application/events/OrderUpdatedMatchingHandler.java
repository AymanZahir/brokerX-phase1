package com.brokerx.application.events;

import com.brokerx.application.MatchOrders;
import org.springframework.stereotype.Component;

@Component
public class OrderUpdatedMatchingHandler implements DomainEventHandler<OrderUpdatedEvent> {

  private final DomainEventBus eventBus;
  private final MatchOrders matchOrders;

  public OrderUpdatedMatchingHandler(DomainEventBus eventBus, MatchOrders matchOrders) {
    this.eventBus = eventBus;
    this.matchOrders = matchOrders;
    eventBus.register(this);
  }

  @Override
  public String eventType() {
    return OrderEvents.ORDER_UPDATED;
  }

  @Override
  public Class<OrderUpdatedEvent> payloadType() {
    return OrderUpdatedEvent.class;
  }

  @Override
  public Object handle(EventEnvelope<OrderUpdatedEvent> envelope) {
    OrderUpdatedEvent event = envelope.payload();
    if (!"WORKING".equalsIgnoreCase(event.status())) {
      return null;
    }
    MatchOrders.MatchResult result = matchOrders.matchOrder(event.orderId());
    OrderMatchedEvent matched =
        new OrderMatchedEvent(
            result.orderId(), result.status(), result.remainingQty(), result.executions());
    eventBus.publish(OrderEvents.ORDER_MATCHED, matched.orderId(), matched);
    return matched;
  }
}
