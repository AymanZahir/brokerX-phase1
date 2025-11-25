package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.VerificationRequestEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRequestJpa extends JpaRepository<VerificationRequestEntity, UUID> {

  Optional<VerificationRequestEntity> findByAccountIdAndStatus(UUID accountId, String status);
}
