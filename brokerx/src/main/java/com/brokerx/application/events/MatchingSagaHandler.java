package com.brokerx.application.events;

import com.brokerx.application.MatchOrders;
import org.springframework.stereotype.Component;

@Component
public class MatchingSagaHandler implements DomainEventHandler<OrderPlacedEvent> {

  private final DomainEventBus eventBus;
  private final MatchOrders matchOrders;

  public MatchingSagaHandler(DomainEventBus eventBus, MatchOrders matchOrders) {
    this.eventBus = eventBus;
    this.matchOrders = matchOrders;
    eventBus.register(this);
  }

  @Override
  public String eventType() {
    return OrderEvents.ORDER_PLACED;
  }

  @Override
  public Class<OrderPlacedEvent> payloadType() {
    return OrderPlacedEvent.class;
  }

  @Override
  public Object handle(EventEnvelope<OrderPlacedEvent> envelope) {
    MatchOrders.MatchResult result = matchOrders.matchOrder(envelope.payload().orderId());
    OrderMatchedEvent matched =
        new OrderMatchedEvent(
            result.orderId(), result.status(), result.remainingQty(), result.executions());
    eventBus.publish(OrderEvents.ORDER_MATCHED, matched.orderId(), matched);
    return matched;
  }
}
