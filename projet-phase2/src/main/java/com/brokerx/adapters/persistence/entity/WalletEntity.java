package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class WalletEntity {

  @Id
  private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "available_balance", nullable = false)
  private BigDecimal availableBalance = BigDecimal.ZERO;

  public WalletEntity() {}

  public WalletEntity(UUID id, UUID accountId, BigDecimal balance) {
    this.id = id;
    this.accountId = accountId;
    this.availableBalance = balance;
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

  public BigDecimal getAvailableBalance() {
    return availableBalance;
  }

  public void setAvailableBalance(BigDecimal availableBalance) {
    this.availableBalance = availableBalance;
  }

  public void credit(BigDecimal amount) {
    this.availableBalance = this.availableBalance.add(amount);
  }
}
