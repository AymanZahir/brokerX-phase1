package com.brokerx.application.events;

public interface DomainEventHandler<T> {

  String eventType();

  Class<T> payloadType();

  Object handle(EventEnvelope<T> envelope);
}
