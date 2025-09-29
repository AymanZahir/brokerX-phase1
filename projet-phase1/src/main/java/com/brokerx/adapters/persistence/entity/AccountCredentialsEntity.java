package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_credentials")
public class AccountCredentialsEntity {

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "mfa_secret")
  private String mfaSecret;

  @Column(name = "failed_attempts", nullable = false)
  private int failedAttempts = 0;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  public AccountCredentialsEntity() {}

  public AccountCredentialsEntity(
      UUID accountId, String passwordHash, String mfaSecret, int failedAttempts, Instant lockedUntil) {
    this.accountId = accountId;
    this.passwordHash = passwordHash;
    this.mfaSecret = mfaSecret;
    this.failedAttempts = failedAttempts;
    this.lockedUntil = lockedUntil;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getMfaSecret() {
    return mfaSecret;
  }

  public void setMfaSecret(String mfaSecret) {
    this.mfaSecret = mfaSecret;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  public void setFailedAttempts(int failedAttempts) {
    this.failedAttempts = failedAttempts;
  }

  public Instant getLockedUntil() {
    return lockedUntil;
  }

  public void setLockedUntil(Instant lockedUntil) {
    this.lockedUntil = lockedUntil;
  }
}
