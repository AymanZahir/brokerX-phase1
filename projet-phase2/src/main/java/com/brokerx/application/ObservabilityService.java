package com.brokerx.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class ObservabilityService {

  private final MeterRegistry registry;

  public ObservabilityService(MeterRegistry registry) {
    this.registry = registry;
  }

  public void recordOrderAccepted(String symbol, String type) {
    Counter.builder("brokerx_orders_accepted_total")
        .description("Total des ordres acceptés")
        .tag("symbol", symbol)
        .tag("type", type)
        .register(registry)
        .increment();
  }

  public Timer.Sample startMatchingSample() {
    return Timer.start(registry);
  }

  public void recordMatching(Timer.Sample sample, String symbol, long matchedQty, int executions, String status) {
    sample.stop(
        Timer.builder("brokerx_matching_duration_seconds")
            .description("Durée du matching d'un ordre")
            .tag("symbol", symbol)
            .tag("executions", Integer.toString(executions))
            .tag("status", status)
            .publishPercentileHistogram()
            .register(registry));

    if (executions > 0) {
      Counter.builder("brokerx_matching_executions_total")
          .description("Nombre d'exécutions générées")
          .tag("symbol", symbol)
          .register(registry)
          .increment(executions);

      DistributionSummary.builder("brokerx_matching_qty")
          .description("Quantité totale appariée")
          .baseUnit("shares")
          .tag("symbol", symbol)
          .register(registry)
          .record(matchedQty);
    }
  }

  public void recordNotification(String type, String channel) {
    Counter.builder("brokerx_notifications_total")
        .description("Notifications émises")
        .tag("type", type)
        .tag("channel", channel)
        .register(registry)
        .increment();
  }
}
