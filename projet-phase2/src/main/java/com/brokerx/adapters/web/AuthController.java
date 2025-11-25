package com.brokerx.adapters.web;

import com.brokerx.application.Authenticate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Profile({"monolith", "auth", "test"})
public class AuthController {

  private final Authenticate authenticate;

  public AuthController(Authenticate authenticate) {
    this.authenticate = authenticate;
  }

  public record LoginDto(
      @Email @NotBlank String email,
      @NotBlank String password,
      String otp) {}

  @PostMapping("/login")
  public Authenticate.Response login(
      @Valid @RequestBody LoginDto dto,
      @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
      @RequestHeader(value = "User-Agent", required = false) String userAgent) {
    var req = new Authenticate.Request(dto.email(), dto.password(), dto.otp(), ip, userAgent);
    return authenticate.handle(req);
  }
}
