package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.entity.AccountCredentialsEntity;
import com.brokerx.adapters.persistence.entity.AccountEntity;
import com.brokerx.adapters.persistence.entity.VerificationRequestEntity;
import com.brokerx.adapters.persistence.entity.WalletEntity;
import com.brokerx.adapters.persistence.repo.AccountCredentialsJpa;
import com.brokerx.adapters.persistence.repo.AccountJpa;
import com.brokerx.adapters.persistence.repo.VerificationRequestJpa;
import com.brokerx.application.events.DomainEventPublisher;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import com.brokerx.adapters.notification.OtpDelivery;
import com.brokerx.application.AuditLogger;
import com.brokerx.application.SignupUser;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SignupUserTest {

  @Mock private AccountJpa accountJpa;
  @Mock private AccountCredentialsJpa credentialsJpa;
  @Mock private VerificationRequestJpa verificationJpa;
  @Mock private WalletJpa walletJpa;
  @Mock private AuditLogger auditLogger;
  @Mock private OtpDelivery otpDelivery;
  @Mock private DomainEventPublisher events;

  private SignupUser signupUser;
  private Clock clock;

  @BeforeEach
  void setup() {
    clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    signupUser =
        new SignupUser(
            accountJpa,
            credentialsJpa,
            verificationJpa,
            walletJpa,
            auditLogger,
            otpDelivery,
            events,
            clock);
  }

  @Test
  void signup_createsAccountAndVerification() {
    when(accountJpa.findByEmail("new@brokerx.dev")).thenReturn(Optional.empty());
    when(accountJpa.findByPhone("+15145550001")).thenReturn(Optional.empty());

    SignupUser.SignupResult result =
        signupUser.signup(
            new SignupUser.SignupCommand(
                "new@brokerx.dev",
                "Password!23",
                "New Trader",
                "+15145550001",
                "123 Demo Street",
                "CA",
                LocalDate.of(1995, 4, 10)));

    assertNotNull(result.accountId());
    assertNotNull(result.verificationId());
    assertEquals(6, result.otp().length());

    ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
    verify(accountJpa).save(accountCaptor.capture());
    AccountEntity savedAccount = accountCaptor.getValue();
    assertEquals("PENDING", savedAccount.getStatus());
    assertEquals("New Trader", savedAccount.getFullName());

    ArgumentCaptor<VerificationRequestEntity> verificationCaptor =
        ArgumentCaptor.forClass(VerificationRequestEntity.class);
    verify(verificationJpa).save(verificationCaptor.capture());
    VerificationRequestEntity verification = verificationCaptor.getValue();
    assertEquals("PENDING", verification.getStatus());
    assertEquals(result.accountId(), verification.getAccountId());

    ArgumentCaptor<AccountCredentialsEntity> credsCaptor =
        ArgumentCaptor.forClass(AccountCredentialsEntity.class);
    verify(credentialsJpa).save(credsCaptor.capture());
    assertEquals(result.accountId(), credsCaptor.getValue().getAccountId());

    ArgumentCaptor<WalletEntity> walletCaptor = ArgumentCaptor.forClass(WalletEntity.class);
    verify(walletJpa).save(walletCaptor.capture());
    assertEquals(result.accountId(), walletCaptor.getValue().getAccountId());

    verify(auditLogger)
        .record(
            eq("AUTH"),
            eq("SIGNUP_PENDING"),
            eq(result.accountId()),
            any(SignupUser.SignupAudit.class));
    verify(otpDelivery).send(eq("new@brokerx.dev"), anyString(), anyString());
  }

  @Test
  void confirm_activatesAccountWhenOtpMatches() {
    UUID accountId = UUID.randomUUID();
    AccountEntity account =
        new AccountEntity(
            accountId,
            "pending@brokerx.dev",
            "PENDING",
            false,
            "Pending User",
            "+15145550002",
            "123 Street",
            "CA",
            LocalDate.of(1990, 1, 1),
            null);
    when(accountJpa.findById(accountId)).thenReturn(Optional.of(account));

    VerificationRequestEntity verification =
        new VerificationRequestEntity(
            UUID.randomUUID(), accountId, "PENDING", "123456", clock.instant().plusSeconds(60));
    when(verificationJpa.findByAccountIdAndStatus(accountId, "PENDING"))
        .thenReturn(Optional.of(verification));

    SignupUser.ConfirmResult result =
        signupUser.confirm(new SignupUser.ConfirmCommand(accountId, "123456"));

    assertEquals("ACTIVE", result.status());
    verify(accountJpa).save(account);
    verify(verificationJpa, atLeastOnce()).save(verification);
    verify(auditLogger)
        .record(
            eq("AUTH"),
            eq("SIGNUP_CONFIRMED"),
            eq(accountId),
            any(SignupUser.ConfirmAudit.class));
  }
}
