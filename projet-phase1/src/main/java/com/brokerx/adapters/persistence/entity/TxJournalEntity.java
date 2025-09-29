package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "tx_journal",
    uniqueConstraints = @UniqueConstraint(columnNames = {"idempotency_key"}))
public class TxJournalEntity {

  @Id
  private UUID id;

  @Column(name = "wallet_id", nullable = false)
  private UUID walletId;

  @Column(nullable = false)
  private String type; // DEPOSIT/WITHDRAW

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String status; // PENDING/SETTLED/FAILED

  @Column(name = "idempotency_key", unique = true)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public TxJournalEntity() {}

  public TxJournalEntity(
      UUID id,
      UUID walletId,
      String type,
      BigDecimal amount,
      String status,
      String idempotencyKey) {
    this.id = id;
    this.walletId = walletId;
    this.type = type;
    this.amount = amount;
    this.status = status;
    this.idempotencyKey = idempotencyKey;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getWalletId() {
    return walletId;
  }

  public void setWalletId(UUID walletId) {
    this.walletId = walletId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
