package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.AuditEventEntity;
import com.brokerx.adapters.persistence.repo.AuditEventJpa;
import com.brokerx.application.support.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogger {

  private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

  private final AuditEventJpa auditEvents;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public AuditLogger(AuditEventJpa auditEvents, ObjectMapper objectMapper, Clock clock) {
    this.auditEvents = auditEvents;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Transactional
  public void record(String category, String action, UUID actorId, Object payload) {
    String traceId = TraceContext.currentTraceId().orElseGet(() -> UUID.randomUUID().toString());
    JsonNode jsonPayload = convertPayload(payload);

    AuditEventEntity event =
        new AuditEventEntity(UUID.randomUUID(), traceId, category, action, actorId, jsonPayload);
    event.setCreatedAt(clock.instant());
    auditEvents.save(event);

    if (log.isDebugEnabled()) {
      log.debug(
          "auditEvent saved traceId={} category={} action={} actorId={}",
          traceId,
          category,
          action,
          actorId);
    }
  }

  private JsonNode convertPayload(Object payload) {
    if (payload == null) {
      return objectMapper.createObjectNode();
    }
    try {
      return objectMapper.valueToTree(payload);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to convert audit payload, falling back to string", e);
      return objectMapper.createObjectNode().put("value", payload.toString());
    }
  }
}
