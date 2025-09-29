package com.brokerx.integration;

import com.brokerx.application.PlaceOrder;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderIntegrationTest {

  @Autowired private PlaceOrder placeOrder;

  private static final UUID ACCOUNT_ID =
      UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Test
  void idempotentOrder_sameClientOrderId_oneAck() {
    var draft =
        new PlaceOrder.Draft(
            ACCOUNT_ID, "BUY", "MARKET", "AAPL", 10, null, "coid-42");
    var ack1 = placeOrder.handle(draft);
    var ack2 = placeOrder.handle(draft);

    Assertions.assertEquals(ack1.orderId(), ack2.orderId(), "Un seul ordre");
    Assertions.assertEquals("WORKING", ack2.status());
  }

  @Test
  void limitOrder_requiresPositivePrice() {
    var draft =
        new PlaceOrder.Draft(
            ACCOUNT_ID, "BUY", "LIMIT", "AAPL", 10, new BigDecimal("-1"), "coid-bad");
    Assertions.assertThrows(IllegalArgumentException.class, () -> placeOrder.handle(draft));
  }
}
