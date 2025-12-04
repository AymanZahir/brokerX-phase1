package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.AccountSessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSessionJpa extends JpaRepository<AccountSessionEntity, UUID> {
  List<AccountSessionEntity> findByAccountId(UUID accountId);
}
