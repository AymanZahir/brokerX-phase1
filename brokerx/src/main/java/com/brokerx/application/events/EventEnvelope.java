package com.brokerx.application.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(UUID id, String type, UUID aggregateId, T payload, Instant createdAt) {}
