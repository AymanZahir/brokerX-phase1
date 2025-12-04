package com.brokerx.adapters.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

  @Id
  private UUID id;

  @Column(name = "trace_id", nullable = false, length = 64)
  private String traceId;

  @Column(nullable = false, length = 64)
  private String category;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(name = "actor_id")
  private UUID actorId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private JsonNode payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public AuditEventEntity() {}

  public AuditEventEntity(
      UUID id,
      String traceId,
      String category,
      String action,
      UUID actorId,
      JsonNode payload) {
    this.id = id;
    this.traceId = traceId;
    this.category = category;
    this.action = action;
    this.actorId = actorId;
    this.payload = payload;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public UUID getActorId() {
    return actorId;
  }

  public void setActorId(UUID actorId) {
    this.actorId = actorId;
  }

  public JsonNode getPayload() {
    return payload;
  }

  public void setPayload(JsonNode payload) {
    this.payload = payload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
