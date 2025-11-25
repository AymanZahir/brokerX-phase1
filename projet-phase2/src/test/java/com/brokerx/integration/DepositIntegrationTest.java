package com.brokerx.integration;

import com.brokerx.application.DepositFunds;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DepositIntegrationTest {

  @Autowired private DepositFunds depositFunds;

  private static final UUID ACCOUNT_ID =
      UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Test
  void idempotentDeposit_sameKey_sameResult_noDoubleCredit() {
    String key = "test-idem-123";
    var first = depositFunds.handle(ACCOUNT_ID, new BigDecimal("100.00"), key);
    var second = depositFunds.handle(ACCOUNT_ID, new BigDecimal("100.00"), key);

    Assertions.assertEquals(first.txId(), second.txId(), "Même transaction");
    Assertions.assertEquals(first.newBalance(), second.newBalance(), "Pas de double crédit");
  }
}
