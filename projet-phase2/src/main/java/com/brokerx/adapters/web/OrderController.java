package com.brokerx.adapters.web;

import com.brokerx.adapters.web.security.JwtAuthenticationFilter.AuthenticatedAccount;
import com.brokerx.application.ModifyOrder;
import com.brokerx.application.PlaceOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/orders")
@Profile({"monolith", "orders", "test"})
public class OrderController {

  private final PlaceOrder usecase;
  private final ModifyOrder modifyOrder;

  public OrderController(PlaceOrder usecase, ModifyOrder modifyOrder) {
    this.usecase = usecase;
    this.modifyOrder = modifyOrder;
  }

  public record PlaceOrderDto(
      @NotNull UUID accountId,
      @NotBlank String side,
      @NotBlank String type,
      @NotBlank String symbol,
      @Positive long qty,
      BigDecimal limitPrice,
      @NotBlank String clientOrderId) {}

  @PostMapping
  public PlaceOrder.Ack place(@Valid @RequestBody PlaceOrderDto dto) {
    UUID current = currentAccountId();
    if (!current.equals(dto.accountId())) {
      throw new AccessDeniedException("ACCOUNT_MISMATCH");
    }
    return usecase.handle(
        new PlaceOrder.Draft(
            dto.accountId(),
            dto.side(),
            dto.type(),
            dto.symbol(),
            dto.qty(),
            dto.limitPrice(),
            dto.clientOrderId()));
  }

  public record ReplaceOrderDto(
      @NotNull Integer expectedVersion,
      Long qty,
      BigDecimal limitPrice,
      String duration) {}

  @PutMapping("/{orderId}")
  public ModifyOrder.OrderMutation replace(
      @PathVariable UUID orderId, @Valid @RequestBody ReplaceOrderDto dto) {
    UUID current = currentAccountId();
    return modifyOrder.replace(
        new ModifyOrder.ReplaceCommand(
            current, orderId, dto.expectedVersion(), dto.qty, dto.limitPrice(), dto.duration()));
  }

  public record CancelOrderDto(@NotNull Integer expectedVersion) {}

  @DeleteMapping("/{orderId}")
  public ModifyOrder.OrderMutation cancel(
      @PathVariable UUID orderId, @Valid @RequestBody CancelOrderDto dto) {
    UUID current = currentAccountId();
    return modifyOrder.cancel(new ModifyOrder.CancelCommand(current, orderId, dto.expectedVersion()));
  }

  private UUID currentAccountId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new AccessDeniedException("UNAUTHENTICATED");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof AuthenticatedAccount account) {
      return account.accountId();
    }
    throw new AccessDeniedException("UNAUTHENTICATED");
  }
}
