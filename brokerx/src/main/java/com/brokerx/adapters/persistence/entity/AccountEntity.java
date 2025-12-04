package com.brokerx.adapters.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "account")
public class AccountEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String status; // PENDING/ACTIVE/...

  @Column(name = "mfa_enabled", nullable = false)
  private boolean mfaEnabled;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "phone", nullable = false, unique = true)
  private String phone;

  @Column(name = "address_line")
  private String addressLine;

  @Column(name = "country")
  private String country;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "kyc_verified_at")
  private Instant kycVerifiedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public AccountEntity() {}

  public AccountEntity(UUID id, String email, String status, boolean mfaEnabled) {
    this(id, email, status, mfaEnabled, "Pending User", "+15550000000", null, null, null, null);
  }

  public AccountEntity(
      UUID id,
      String email,
      String status,
      boolean mfaEnabled,
      String fullName,
      String phone,
      String addressLine,
      String country,
      LocalDate dateOfBirth,
      Instant kycVerifiedAt) {
    this.id = id;
    this.email = email;
    this.status = status;
    this.mfaEnabled = mfaEnabled;
    this.fullName = fullName;
    this.phone = phone;
    this.addressLine = addressLine;
    this.country = country;
    this.dateOfBirth = dateOfBirth;
    this.kycVerifiedAt = kycVerifiedAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isMfaEnabled() {
    return mfaEnabled;
  }

  public void setMfaEnabled(boolean mfaEnabled) {
    this.mfaEnabled = mfaEnabled;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAddressLine() {
    return addressLine;
  }

  public void setAddressLine(String addressLine) {
    this.addressLine = addressLine;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public Instant getKycVerifiedAt() {
    return kycVerifiedAt;
  }

  public void setKycVerifiedAt(Instant kycVerifiedAt) {
    this.kycVerifiedAt = kycVerifiedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
