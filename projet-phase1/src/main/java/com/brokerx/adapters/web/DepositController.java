package com.brokerx.adapters.web;

import com.brokerx.application.DepositFunds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/deposits")
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
    return usecase.handle(dto.accountId(), dto.amount(), dto.idempotencyKey());
  }
}
