package com.brokerx.application.events;

import java.util.List;
import java.util.UUID;

public interface DomainEventPublisher {

  List<Object> publish(String eventType, UUID aggregateId, Object payload);
}
