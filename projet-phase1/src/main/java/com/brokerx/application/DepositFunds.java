package com.brokerx.application;

import com.brokerx.adapters.persistence.entity.TxJournalEntity;
import com.brokerx.adapters.persistence.entity.WalletEntity;
import com.brokerx.adapters.persistence.repo.TxJournalJpa;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositFunds {

  private final WalletJpa wallets;
  private final TxJournalJpa txs;

  public record Result(BigDecimal newBalance, UUID txId) {}

  public DepositFunds(WalletJpa wallets, TxJournalJpa txs) {
    this.wallets = wallets;
    this.txs = txs;
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

    return new Result(wallet.getAvailableBalance(), pending.getId());
  }
}
