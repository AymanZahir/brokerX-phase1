package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.WalletEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpa extends JpaRepository<WalletEntity, UUID> {
  Optional<WalletEntity> findByAccountId(UUID accountId);
}
