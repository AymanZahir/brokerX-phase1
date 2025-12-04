package com.brokerx.adapters.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class MarketDataController {

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private static final Random random = new Random();

  @GetMapping(path = "/api/v1/market/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamQuotes(@RequestParam(defaultValue = "AAPL") String symbol) {
    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(1));
    double[] price = {100.0 + random.nextDouble()};

    Runnable task =
        () -> {
          try {
            price[0] = Math.max(1.0, price[0] + (random.nextDouble() - 0.5));
            emitter.send(
                Map.of(
                    "symbol", symbol.toUpperCase(),
                    "price", BigDecimal.valueOf(price[0]).setScale(2, java.math.RoundingMode.HALF_UP),
                    "ts", Instant.now().toString()));
          } catch (IOException e) {
            emitter.completeWithError(e);
          }
        };

    var future = scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    emitter.onCompletion(() -> future.cancel(true));
    emitter.onTimeout(() -> future.cancel(true));
    return emitter;
  }
}
