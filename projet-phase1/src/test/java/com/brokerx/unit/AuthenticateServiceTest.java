package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.entity.AccountCredentialsEntity;
import com.brokerx.adapters.persistence.entity.AccountEntity;
import com.brokerx.adapters.persistence.entity.AccountSessionEntity;
import com.brokerx.adapters.persistence.repo.AccountCredentialsJpa;
import com.brokerx.adapters.persistence.repo.AccountJpa;
import com.brokerx.adapters.persistence.repo.AccountSessionJpa;
import com.brokerx.application.Authenticate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticateServiceTest {

  @Mock private AccountJpa accountJpa;
  @Mock private AccountCredentialsJpa credentialsJpa;
  @Mock private AccountSessionJpa sessionJpa;

  private Clock clock;
  private Authenticate authenticate;

  private static final UUID ACCOUNT_ID = UUID.randomUUID();

  @BeforeEach
  void setup() {
    clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    authenticate = new Authenticate(accountJpa, credentialsJpa, sessionJpa, clock);

    AccountEntity account = new AccountEntity(ACCOUNT_ID, "user@test", "ACTIVE", true);
    when(accountJpa.findByEmail("user@test")).thenReturn(Optional.of(account));
  }

  @Test
  void authenticate_successfulLogin_createsSessionAndResetsFailures() {
    AccountCredentialsEntity creds =
        new AccountCredentialsEntity(ACCOUNT_ID, "secret", "000000", 2, Instant.parse("2024-12-31T00:00:00Z"));
    when(credentialsJpa.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(creds));

    Authenticate.Response response =
        authenticate.handle(new Authenticate.Request("user@test", "secret", "000000", null, null));

    assertNotNull(response.sessionId());
    verify(sessionJpa).save(any(AccountSessionEntity.class));
    ArgumentCaptor<AccountCredentialsEntity> credCaptor =
        ArgumentCaptor.forClass(AccountCredentialsEntity.class);
    verify(credentialsJpa, atLeastOnce()).save(credCaptor.capture());
    AccountCredentialsEntity saved = credCaptor.getValue();
    assertEquals(0, saved.getFailedAttempts());
    assertNull(saved.getLockedUntil());
  }

  @Test
  void authenticate_badPassword_incrementsFailuresAndLocks() {
    AccountCredentialsEntity creds =
        new AccountCredentialsEntity(ACCOUNT_ID, "secret", "000000", 4, null);
    when(credentialsJpa.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(creds));

    assertThrows(
        IllegalArgumentException.class,
        () -> authenticate.handle(new Authenticate.Request("user@test", "wrong", "000000", null, null)));

    ArgumentCaptor<AccountCredentialsEntity> captor =
        ArgumentCaptor.forClass(AccountCredentialsEntity.class);
    verify(credentialsJpa).save(captor.capture());
    AccountCredentialsEntity updated = captor.getValue();
    assertTrue(updated.getFailedAttempts() >= 5);
    assertNotNull(updated.getLockedUntil());
    verify(sessionJpa, never()).save(any());
  }

  @Test
  void authenticate_missingOtpWhenRequired_throws() {
    AccountCredentialsEntity creds =
        new AccountCredentialsEntity(ACCOUNT_ID, "secret", "000000", 0, null);
    when(credentialsJpa.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(creds));

    assertThrows(
        IllegalArgumentException.class,
        () -> authenticate.handle(new Authenticate.Request("user@test", "secret", null, null, null)));
    verify(sessionJpa, never()).save(any());
  }
}
