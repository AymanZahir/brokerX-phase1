package com.brokerx.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ScenarioE2ETest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void loginDepositOrder_endToEnd_succeeds() {
    ResponseEntity<LoginResponse> loginResponse =
        restTemplate.postForEntity(
            "/internal/auth/login",
            Map.of(
                "email", "seed@brokerx.dev",
                "password", "password123",
                "otp", "123456"),
            LoginResponse.class);
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();

    ResponseEntity<DepositResponse> depositResponse =
        restTemplate.postForEntity(
            "/internal/deposits",
            Map.of(
                "accountId", "11111111-1111-1111-1111-111111111111",
                "amount", new BigDecimal("50.00"),
                "idempotencyKey", "scenario-" + UUID.randomUUID()),
            DepositResponse.class);
    assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(depositResponse.getBody()).isNotNull();
    assertThat(depositResponse.getBody().newBalance()).isGreaterThan(BigDecimal.ZERO);

    ResponseEntity<OrderResponse> orderResponse =
        restTemplate.postForEntity(
            "/internal/orders",
            Map.of(
                "accountId", "11111111-1111-1111-1111-111111111111",
                "side", "BUY",
                "type", "MARKET",
                "symbol", "AAPL",
                "qty", 1,
                "clientOrderId", "scenario-order-" + UUID.randomUUID()),
            OrderResponse.class);
    assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(orderResponse.getBody()).isNotNull();
    assertThat(orderResponse.getBody().status()).isEqualTo("WORKING");
  }

  record LoginResponse(UUID sessionId) {}

  record DepositResponse(BigDecimal newBalance, UUID txId) {}

  record OrderResponse(UUID orderId, String status) {}
}
