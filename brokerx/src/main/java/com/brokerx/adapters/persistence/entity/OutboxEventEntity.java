package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  private String type;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "error_message")
  private String errorMessage;

  public OutboxEventEntity() {}

  public OutboxEventEntity(
      UUID id,
      String type,
      UUID aggregateId,
      String payload,
      String status,
      Instant createdAt) {
    this.id = id;
    this.type = type;
    this.aggregateId = aggregateId;
    this.payload = payload;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public UUID getAggregateId() {
    return aggregateId;
  }

  public String getPayload() {
    return payload;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }

  public void setProcessedAt(Instant processedAt) {
    this.processedAt = processedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
