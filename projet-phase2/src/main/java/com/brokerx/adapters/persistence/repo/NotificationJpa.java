package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpa extends JpaRepository<NotificationEntity, UUID> {
  List<NotificationEntity> findByOrderId(UUID orderId);

  List<NotificationEntity> findTop20ByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
