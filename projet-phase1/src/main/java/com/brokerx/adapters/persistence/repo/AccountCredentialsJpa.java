package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.AccountCredentialsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountCredentialsJpa extends JpaRepository<AccountCredentialsEntity, UUID> {
  Optional<AccountCredentialsEntity> findByAccountId(UUID accountId);
}
