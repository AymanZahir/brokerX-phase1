package com.brokerx.adapters.web;

import com.brokerx.application.PlaceOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/orders")
public class OrderController {

  private final PlaceOrder usecase;

  public OrderController(PlaceOrder usecase) {
    this.usecase = usecase;
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
}
