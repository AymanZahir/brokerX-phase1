package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.TxJournalEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxJournalJpa extends JpaRepository<TxJournalEntity, UUID> {
  Optional<TxJournalEntity> findByIdempotencyKey(String idempotencyKey);
}
