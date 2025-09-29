package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.AccountCredentialsEntity;
import com.brokerx.adapters.persistence.entity.AccountEntity;
import com.brokerx.adapters.persistence.entity.AccountSessionEntity;
import com.brokerx.adapters.persistence.repo.AccountCredentialsJpa;
import com.brokerx.adapters.persistence.repo.AccountJpa;
import com.brokerx.adapters.persistence.repo.AccountSessionJpa;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Authenticate {

  private final AccountJpa accounts;
  private final AccountCredentialsJpa credentials;
  private final AccountSessionJpa sessions;
  private final Clock clock;

  public Authenticate(
      AccountJpa accounts, AccountCredentialsJpa credentials, AccountSessionJpa sessions, Clock clock) {
    this.accounts = accounts;
    this.credentials = credentials;
    this.sessions = sessions;
    this.clock = clock;
  }

  public record Request(String email, String password, String otp, String ip, String userAgent) {}

  public record Response(UUID sessionId, Instant expiresAt) {}

  @Transactional
  public Response handle(Request request) {
    AccountEntity account =
        accounts.findByEmail(request.email()).orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));

    if (!"ACTIVE".equals(account.getStatus())) {
      throw new IllegalStateException("ACCOUNT_NOT_ACTIVE");
    }

    AccountCredentialsEntity cred =
        credentials
            .findByAccountId(account.getId())
            .orElseThrow(() -> new IllegalStateException("CREDENTIALS_MISSING"));

    Instant now = clock.instant();
    if (cred.getLockedUntil() != null && cred.getLockedUntil().isAfter(now)) {
      throw new IllegalStateException("ACCOUNT_LOCKED");
    }

    if (!cred.getPasswordHash().equals(request.password())) {
      cred.setFailedAttempts(cred.getFailedAttempts() + 1);
      if (cred.getFailedAttempts() >= 5) {
        cred.setLockedUntil(now.plus(Duration.ofMinutes(15)));
      }
      credentials.save(cred);
      throw new IllegalArgumentException("INVALID_CREDENTIALS");
    }

    if (account.isMfaEnabled()) {
      String expectedOtp = Optional.ofNullable(cred.getMfaSecret()).orElse("");
      if (request.otp() == null || !expectedOtp.equals(request.otp())) {
        throw new IllegalArgumentException("MFA_REQUIRED");
      }
    }

    cred.setFailedAttempts(0);
    cred.setLockedUntil(null);
    credentials.save(cred);

    UUID sessionId = UUID.randomUUID();
    Instant expiresAt = now.plus(Duration.ofHours(1));
    AccountSessionEntity session = new AccountSessionEntity(sessionId, account.getId(), now, expiresAt);
    session.setLastIp(request.ip());
    session.setUserAgent(request.userAgent());
    sessions.save(session);

    return new Response(sessionId, expiresAt);
  }
}
