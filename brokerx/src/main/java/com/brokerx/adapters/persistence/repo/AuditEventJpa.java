package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.AuditEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpa extends JpaRepository<AuditEventEntity, UUID> {}
