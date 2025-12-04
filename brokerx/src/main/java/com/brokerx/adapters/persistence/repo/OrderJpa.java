package com.brokerx.adapters.persistence.repo;

import com.brokerx.adapters.persistence.entity.OrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpa extends JpaRepository<OrderEntity, UUID> {
  Optional<OrderEntity> findByClientOrderId(String clientOrderId);
}
