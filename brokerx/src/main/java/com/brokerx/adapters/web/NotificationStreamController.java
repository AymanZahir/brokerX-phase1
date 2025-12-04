package com.brokerx.adapters.web;

import com.brokerx.adapters.persistence.entity.NotificationEntity;
import com.brokerx.adapters.persistence.repo.NotificationJpa;
import com.brokerx.adapters.web.security.JwtAuthenticationFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationStreamController {

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private final NotificationJpa notifications;

  public NotificationStreamController(NotificationJpa notifications) {
    this.notifications = notifications;
  }

  @GetMapping(path = "/api/v1/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamNotifications(@RequestParam(required = false) UUID accountId) {
    UUID current = currentAccountId();
    UUID target = accountId != null ? accountId : current;
    if (!current.equals(target)) {
      throw new AccessDeniedException("ACCOUNT_MISMATCH");
    }

    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(1));
    Instant[] lastSeen = {Instant.MIN};

    Runnable task =
        () -> {
          try {
            List<NotificationEntity> recent =
                notifications.findTop20ByAccountIdOrderByCreatedAtDesc(target).stream()
                    .filter(n -> n.getCreatedAt().isAfter(lastSeen[0]))
                    .toList();
            if (!recent.isEmpty()) {
              lastSeen[0] = recent.get(0).getCreatedAt();
              emitter.send(
                  recent.stream()
                      .map(
                          n ->
                              Map.of(
                                  "id", n.getId(),
                                  "accountId", n.getAccountId(),
                                  "orderId", n.getOrderId(),
                                  "type", n.getType(),
                                  "channel", n.getChannel(),
                                  "payload", n.getPayload(),
                                  "createdAt", n.getCreatedAt().toString()))
                      .toList());
            }
          } catch (IOException e) {
            emitter.completeWithError(e);
          }
        };

    var future = scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    emitter.onCompletion(() -> future.cancel(true));
    emitter.onTimeout(() -> future.cancel(true));
    return emitter;
  }

  private UUID currentAccountId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new AccessDeniedException("UNAUTHENTICATED");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof JwtAuthenticationFilter.AuthenticatedAccount account) {
      return account.accountId();
    }
    throw new AccessDeniedException("UNAUTHENTICATED");
  }
}
