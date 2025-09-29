package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.AccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpa extends JpaRepository<AccountEntity, UUID> {
  Optional<AccountEntity> findByEmail(String email);
}
