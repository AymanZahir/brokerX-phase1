package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.repo.OutboxEventJpa;
import com.brokerx.application.MatchOrders;
import com.brokerx.application.events.DomainEventBus;
import com.brokerx.application.events.MatchingSagaHandler;
import com.brokerx.application.events.OrderEvents;
import com.brokerx.application.events.OrderMatchedEvent;
import com.brokerx.application.events.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class MatchingSagaHandlerTest {

  @Mock private OutboxEventJpa outboxEventJpa;
  @Mock private MatchOrders matchOrders;

  private DomainEventBus eventBus;

  @BeforeEach
  void setup() {
    when(outboxEventJpa.save(any())).thenAnswer((Answer<Object>) invocation -> invocation.getArgument(0));
    eventBus =
        new DomainEventBus(
            outboxEventJpa, new ObjectMapper(), Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC));
    new MatchingSagaHandler(eventBus, matchOrders);
  }

  @Test
  void publishOrderPlaced_dispatchesMatching() {
    UUID orderId = UUID.randomUUID();
    when(matchOrders.matchOrder(orderId))
        .thenReturn(new MatchOrders.MatchResult(orderId, "WORKING", 0, List.of()));

    List<Object> responses =
        eventBus.publish(
            OrderEvents.ORDER_PLACED,
            orderId,
            new OrderPlacedEvent(orderId, UUID.randomUUID(), "BUY", "MARKET", "AAPL", 1, null));

    verify(matchOrders).matchOrder(orderId);
    verify(outboxEventJpa, atLeastOnce()).save(any());
    assertEquals(1, responses.size());
    assertTrue(responses.get(0) instanceof OrderMatchedEvent);
    assertEquals("WORKING", ((OrderMatchedEvent) responses.get(0)).status());
  }
}
