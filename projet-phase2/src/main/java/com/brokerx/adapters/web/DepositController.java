package com.brokerx.adapters.web;

import com.brokerx.adapters.web.security.JwtAuthenticationFilter.AuthenticatedAccount;
import com.brokerx.application.DepositFunds;
import org.springframework.context.annotation.Profile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deposits")
@Profile({"monolith", "portfolio", "test"})
public class DepositController {

  private final DepositFunds usecase;

  public DepositController(DepositFunds usecase) {
    this.usecase = usecase;
  }

  public record DepositDto(
      @NotNull UUID accountId,
      @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
      @NotBlank String idempotencyKey) {}

  @PostMapping
  public DepositFunds.Result deposit(@Valid @RequestBody DepositDto dto) {
    UUID currentAccount = currentAccountId();
    if (!currentAccount.equals(dto.accountId())) {
      throw new AccessDeniedException("ACCOUNT_MISMATCH");
    }
    return usecase.handle(dto.accountId(), dto.amount(), dto.idempotencyKey());
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
