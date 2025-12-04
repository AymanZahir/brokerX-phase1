package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(columnNames = {"client_order_id"}))
public class OrderEntity {

  @Id
  private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private String side; // BUY/SELL

  @Column(nullable = false)
  private String type; // MARKET/LIMIT

  @Column(nullable = false)
  private String symbol;

  @Column(nullable = false)
  private long qty;

  @Column(name = "limit_price")
  private BigDecimal limitPrice; // null if MARKET

  @Column(nullable = false)
  private String status; // NEW/WORKING/...

  @Column(name = "client_order_id", unique = true)
  private String clientOrderId;

  @Version
  private int version = 0;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public OrderEntity() {}

  public OrderEntity(
      UUID id,
      UUID accountId,
      String side,
      String type,
      String symbol,
      long qty,
      BigDecimal limit,
      String clientOrderId,
      String status) {
    this.id = id;
    this.accountId = accountId;
    this.side = side;
    this.type = type;
    this.symbol = symbol;
    this.qty = qty;
    this.limitPrice = limit;
    this.clientOrderId = clientOrderId;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
  }

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public long getQty() {
    return qty;
  }

  public void setQty(long qty) {
    this.qty = qty;
  }

  public BigDecimal getLimitPrice() {
    return limitPrice;
  }

  public void setLimitPrice(BigDecimal limitPrice) {
    this.limitPrice = limitPrice;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getClientOrderId() {
    return clientOrderId;
  }

  public void setClientOrderId(String clientOrderId) {
    this.clientOrderId = clientOrderId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
