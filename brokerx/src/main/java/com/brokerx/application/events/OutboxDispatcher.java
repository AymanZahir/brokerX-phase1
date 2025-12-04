package com.brokerx.application.events;

import com.brokerx.adapters.persistence.repo.OutboxEventJpa;
import com.brokerx.application.ObservabilityService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxDispatcher {

  private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

  private final DomainEventBus eventBus;
  private final OutboxEventJpa outbox;
  private final ObservabilityService observabilityService;
  private final AtomicInteger pendingGauge;

  public OutboxDispatcher(
      DomainEventBus eventBus, OutboxEventJpa outbox, ObservabilityService observabilityService, MeterRegistry registry) {
    this.eventBus = eventBus;
    this.outbox = outbox;
    this.observabilityService = observabilityService;
    this.pendingGauge = registry.gauge("brokerx_outbox_pending", new AtomicInteger(0));
  }

  @Scheduled(fixedDelayString = "${brokerx.outbox.dispatch-interval-ms:5000}")
  public void dispatchPending() {
    int pending = outbox.findByStatus("PENDING").size();
    pendingGauge.set(pending);
    if (pending == 0) {
      return;
    }
    try {
      var results = eventBus.replayPending();
      observabilityService.recordOutboxProcessed(pending, 0);
      log.debug("Outbox replay processed {} events, results={}", pending, results.size());
    } catch (Exception e) {
      observabilityService.recordOutboxProcessed(0, 1);
      log.error("Outbox dispatch failed: {}", e.getMessage(), e);
    }
  }
}
