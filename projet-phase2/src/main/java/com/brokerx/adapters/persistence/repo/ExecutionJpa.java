package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.ExecutionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionJpa extends JpaRepository<ExecutionEntity, UUID> {
  List<ExecutionEntity> findByOrderId(UUID orderId);
}
