package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution")
public class ExecutionEntity {

  @Id private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "counter_order_id")
  private UUID counterOrderId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "counter_account_id")
  private UUID counterAccountId;

  @Column(nullable = false)
  private String symbol;

  @Column(nullable = false)
  private long qty;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private String side;

  @Column(name = "execution_time", nullable = false)
  private Instant executionTime = Instant.now();

  public ExecutionEntity() {}

  public ExecutionEntity(
      UUID id,
      UUID orderId,
      UUID counterOrderId,
      UUID accountId,
      UUID counterAccountId,
      String symbol,
      long qty,
      BigDecimal price,
      String side) {
    this.id = id;
    this.orderId = orderId;
    this.counterOrderId = counterOrderId;
    this.accountId = accountId;
    this.counterAccountId = counterAccountId;
    this.symbol = symbol;
    this.qty = qty;
    this.price = price;
    this.side = side;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public void setOrderId(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getCounterOrderId() {
    return counterOrderId;
  }

  public void setCounterOrderId(UUID counterOrderId) {
    this.counterOrderId = counterOrderId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
  }

  public UUID getCounterAccountId() {
    return counterAccountId;
  }

  public void setCounterAccountId(UUID counterAccountId) {
    this.counterAccountId = counterAccountId;
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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  public Instant getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(Instant executionTime) {
    this.executionTime = executionTime;
  }
}
