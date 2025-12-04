package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.AccountCredentialsEntity;
import com.brokerx.adapters.persistence.entity.AccountEntity;
import com.brokerx.adapters.persistence.entity.VerificationRequestEntity;
import com.brokerx.adapters.persistence.entity.WalletEntity;
import com.brokerx.adapters.persistence.repo.AccountCredentialsJpa;
import com.brokerx.adapters.persistence.repo.AccountJpa;
import com.brokerx.adapters.persistence.repo.VerificationRequestJpa;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import com.brokerx.adapters.notification.OtpDelivery;
import com.brokerx.application.events.DomainEventPublisher;
import com.brokerx.application.events.EventTopics;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupUser {

  private static final Logger log = LoggerFactory.getLogger(SignupUser.class);

  private final AccountJpa accounts;
  private final AccountCredentialsJpa credentials;
  private final VerificationRequestJpa verifications;
  private final WalletJpa wallets;
  private final AuditLogger auditLogger;
  private final OtpDelivery otpDelivery;
  private final DomainEventPublisher events;
  private final Clock clock;
  private final SecureRandom random = new SecureRandom();

  public SignupUser(
      AccountJpa accounts,
      AccountCredentialsJpa credentials,
      VerificationRequestJpa verifications,
      WalletJpa wallets,
      AuditLogger auditLogger,
      OtpDelivery otpDelivery,
      DomainEventPublisher events,
      Clock clock) {
    this.accounts = accounts;
    this.credentials = credentials;
    this.verifications = verifications;
    this.wallets = wallets;
    this.auditLogger = auditLogger;
    this.otpDelivery = otpDelivery;
    this.events = events;
    this.clock = clock;
  }

  public record SignupCommand(
      String email,
      String password,
      String fullName,
      String phone,
      String address,
      String country,
      LocalDate dateOfBirth) {}

  public record SignupResult(UUID accountId, UUID verificationId, String otp) {}

  public record ConfirmCommand(UUID accountId, String otp) {}

  public record ConfirmResult(UUID accountId, String status) {}

  @Transactional
  public SignupResult signup(SignupCommand command) {
    String email = command.email().trim().toLowerCase(Locale.ROOT);
    accounts
        .findByEmail(email)
        .ifPresent(
            existing -> {
              throw new IllegalStateException("ACCOUNT_EMAIL_EXISTS");
            });
    accounts
        .findByPhone(command.phone())
        .ifPresent(
            existing -> {
              throw new IllegalStateException("ACCOUNT_PHONE_EXISTS");
            });

    UUID accountId = UUID.randomUUID();
    AccountEntity account =
        new AccountEntity(
            accountId,
            email,
            "PENDING",
            false,
            command.fullName(),
            command.phone(),
            command.address(),
            command.country(),
            command.dateOfBirth(),
            null);
    accounts.save(account);

    AccountCredentialsEntity creds =
        new AccountCredentialsEntity(accountId, command.password(), null, 0, null);
    credentials.save(creds);

    WalletEntity wallet = new WalletEntity(UUID.randomUUID(), accountId, BigDecimal.ZERO);
    wallets.save(wallet);

    String otp = generateOtp();
    VerificationRequestEntity verification =
        new VerificationRequestEntity(
            UUID.randomUUID(), accountId, "PENDING", otp, clock.instant().plus(Duration.ofMinutes(15)));
    verifications.save(verification);

    try {
      otpDelivery.send(email, otp, verification.getId().toString());
    } catch (Exception e) {
      log.warn("Échec d'envoi de l'OTP par email pour {}", email, e);
    }

    auditLogger.record(
        "AUTH",
        "SIGNUP_PENDING",
        accountId,
        new SignupAudit(accountId, command.fullName(), command.phone(), verification.getId()));

    publishAccountCreated(accountId, email);

    return new SignupResult(accountId, verification.getId(), otp);
  }

  @Transactional
  public ConfirmResult confirm(ConfirmCommand command) {
    AccountEntity account =
        accounts
            .findById(command.accountId())
            .orElseThrow(() -> new IllegalArgumentException("ACCOUNT_NOT_FOUND"));

    VerificationRequestEntity verification =
        verifications
            .findByAccountIdAndStatus(account.getId(), "PENDING")
            .orElseThrow(() -> new IllegalStateException("VERIFICATION_NOT_FOUND"));

    Instant now = clock.instant();
    if (verification.getExpiresAt().isBefore(now)) {
      verification.setStatus("EXPIRED");
      verification.setCompletedAt(now);
      verifications.save(verification);
      throw new IllegalStateException("VERIFICATION_EXPIRED");
    }

    if (!verification.getOtp().equals(command.otp())) {
      verification.setAttempts(verification.getAttempts() + 1);
      verifications.save(verification);
      throw new IllegalArgumentException("INVALID_OTP");
    }

    verification.setStatus("APPROVED");
    verification.setCompletedAt(now);
    verifications.save(verification);

    account.setStatus("ACTIVE");
    account.setKycVerifiedAt(now);
    accounts.save(account);

    auditLogger.record(
        "AUTH", "SIGNUP_CONFIRMED", account.getId(), new ConfirmAudit(account.getId(), verification.getId()));

    return new ConfirmResult(account.getId(), account.getStatus());
  }

  private String generateOtp() {
    int value = random.nextInt(900000) + 100000;
    return Integer.toString(value);
  }

  public record SignupAudit(UUID accountId, String fullName, String phone, UUID verificationId) {}

  public record ConfirmAudit(UUID accountId, UUID verificationId) {}

  private void publishAccountCreated(UUID accountId, String email) {
    try {
      String payload =
          """
          {"accountId":"%s","email":"%s","status":"PENDING","createdAt":"%s"}
          """
              .formatted(accountId, email, clock.instant());
      events.publish(EventTopics.ACCOUNT_CREATED, payload);
    } catch (Exception e) {
      // ne bloque pas le flux si l'envoi event échoue
    }
  }
}
