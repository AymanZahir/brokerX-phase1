package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.OutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpa extends JpaRepository<OutboxEventEntity, UUID> {

  List<OutboxEventEntity> findByStatus(String status);
}
