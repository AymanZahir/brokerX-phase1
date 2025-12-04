package com.brokerx.integration;

import com.brokerx.application.Authenticate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthIntegrationTest {

  @Autowired private Authenticate authenticate;

  @Test
  void login_withValidCredentialsAndOtp_createsSession() {
    var response =
        authenticate.handle(
            new Authenticate.Request(
                "seed@brokerx.dev", "password123", "123456", "127.0.0.1", "JUnit"));
    Assertions.assertNotNull(response.sessionId());
    Assertions.assertNotNull(response.expiresAt());
    Assertions.assertNotNull(response.token());
  }

  @Test
  void login_withInvalidPassword_throwsError() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            authenticate.handle(
                new Authenticate.Request(
                    "seed@brokerx.dev", "wrong", "123456", "127.0.0.1", "JUnit")));
  }

  @Test
  void login_missingOtp_whenMfaEnabled_throwsError() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            authenticate.handle(
                new Authenticate.Request(
                    "seed@brokerx.dev", "password123", null, "127.0.0.1", "JUnit")));
  }
}
