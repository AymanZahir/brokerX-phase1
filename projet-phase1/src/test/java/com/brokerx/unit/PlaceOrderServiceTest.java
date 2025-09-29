package com.brokerx.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brokerx.adapters.persistence.entity.OrderEntity;
import com.brokerx.adapters.persistence.entity.WalletEntity;
import com.brokerx.adapters.persistence.repo.OrderJpa;
import com.brokerx.adapters.persistence.repo.WalletJpa;
import com.brokerx.application.PlaceOrder;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

  @Mock private OrderJpa orderJpa;
  @Mock private WalletJpa walletJpa;

  private PlaceOrder placeOrder;

  @BeforeEach
  void setup() {
    placeOrder = new PlaceOrder(orderJpa, walletJpa);
  }

  @Test
  void handle_returnsExistingAck_whenIdempotent() {
    UUID orderId = UUID.randomUUID();
    OrderEntity existing =
        new OrderEntity(
            orderId,
            UUID.randomUUID(),
            "BUY",
            "MARKET",
            "AAPL",
            10,
            null,
            "dup",
            "WORKING");

    when(orderJpa.findByClientOrderId("dup")).thenReturn(Optional.of(existing));

    PlaceOrder.Ack ack =
        placeOrder.handle(
            new PlaceOrder.Draft(
                existing.getAccountId(), "BUY", "MARKET", "AAPL", 10, null, "dup"));

    assertEquals(orderId, ack.orderId());
    verify(orderJpa, never()).save(any());
  }

  @Test
  void handle_throwsWhenQuantityZero() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            placeOrder.handle(
                new PlaceOrder.Draft(
                    UUID.randomUUID(), "BUY", "MARKET", "AAPL", 0, null, "q0")));
  }

  @Test
  void handle_throwsWhenLimitPriceInvalid() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            placeOrder.handle(
                new PlaceOrder.Draft(
                    UUID.randomUUID(), "BUY", "LIMIT", "AAPL", 1, BigDecimal.valueOf(-1), "bad")));
  }

  @Test
  void handle_savesOrder_whenValid() {
    UUID accountId = UUID.randomUUID();
    when(orderJpa.findByClientOrderId("ok")).thenReturn(Optional.empty());
    when(walletJpa.findByAccountId(accountId))
        .thenReturn(Optional.of(new WalletEntity(UUID.randomUUID(), accountId, BigDecimal.ZERO)));

    placeOrder.handle(
        new PlaceOrder.Draft(accountId, "BUY", "MARKET", "AAPL", 5, null, "ok"));

    ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
    verify(orderJpa).save(captor.capture());
    assertEquals("WORKING", captor.getValue().getStatus());
    assertEquals("AAPL", captor.getValue().getSymbol());
  }
}
