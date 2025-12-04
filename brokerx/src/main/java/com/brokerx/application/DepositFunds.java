package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.TxJournalEntity;
import com.brokerx.adapters.persistence.entity.WalletEntity;
import com.brokerx.adapters.persistence.repo.TxJournalJpa;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import com.brokerx.application.events.DomainEventPublisher;
import com.brokerx.application.events.EventTopics;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositFunds {

  private final WalletJpa wallets;
  private final TxJournalJpa txs;
  private final AuditLogger auditLogger;
  private final DomainEventPublisher events;

  public record Result(BigDecimal newBalance, UUID txId) {}

  public DepositFunds(
      WalletJpa wallets, TxJournalJpa txs, AuditLogger auditLogger, DomainEventPublisher events) {
    this.wallets = wallets;
    this.txs = txs;
    this.auditLogger = auditLogger;
    this.events = events;
  }

  @Transactional
  public Result handle(UUID accountId, BigDecimal amount, String idempotencyKey) {
    Optional<TxJournalEntity> prior = txs.findByIdempotencyKey(idempotencyKey);
    if (prior.isPresent()) {
      UUID walletId = prior.get().getWalletId();
      WalletEntity wallet = wallets.findById(walletId).orElseThrow();
      return new Result(wallet.getAvailableBalance(), prior.get().getId());
    }

    WalletEntity wallet = wallets.findByAccountId(accountId).orElseThrow();
    TxJournalEntity pending =
        new TxJournalEntity(
            UUID.randomUUID(), wallet.getId(), "DEPOSIT", amount, "PENDING", idempotencyKey);
    txs.save(pending);

    pending.setStatus("SETTLED");
    txs.save(pending);

    wallet.credit(amount);
    wallets.save(wallet);

    Result result = new Result(wallet.getAvailableBalance(), pending.getId());
    auditLogger.record(
        "WALLET",
        "DEPOSIT_SETTLED",
        accountId,
        new DepositAudit(wallet.getId(), pending.getId(), amount));
    publishDepositValidated(accountId, wallet.getAvailableBalance(), pending.getId(), amount);
    return result;
  }

  public record DepositAudit(UUID walletId, UUID txId, BigDecimal amount) {}

  private void publishDepositValidated(UUID accountId, BigDecimal balance, UUID txId, BigDecimal amount) {
    try {
      String payload =
          """
          {"depositId":"%s","accountId":"%s","amount":%s,"balance":%s}
          """
              .formatted(txId, accountId, amount, balance);
      events.publish(EventTopics.DEPOSIT_VALIDATED, payload);
    } catch (Exception e) {
      // ne bloque pas le flux si l'envoi event échoue
    }
  }
}
