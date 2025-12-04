package com.brokerx.adapters.web;

import com.brokerx.application.SignupUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Profile({"monolith", "auth", "test"})
public class SignupController {

  private final SignupUser signupUser;
  private final Environment environment;
  private final boolean exposeOtpResponse;

  public SignupController(
      SignupUser signupUser,
      Environment environment,
      @Value("${brokerx.mail.expose-otp-response:false}") boolean exposeOtpResponse) {
    this.signupUser = signupUser;
    this.environment = environment;
    this.exposeOtpResponse = exposeOtpResponse;
  }

  public record SignupDto(
      @Email @NotBlank String email,
      @NotBlank String password,
      @NotBlank String fullName,
      @NotBlank String phone,
      @NotBlank String address,
      @NotBlank String country,
      @NotNull LocalDate dateOfBirth) {}

  public record ConfirmDto(
      @NotBlank @Pattern(regexp = "^[0-9a-fA-F-]{36}$") String accountId,
      @NotBlank String otp) {}

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupDto dto) {
    SignupUser.SignupResult result =
        signupUser.signup(
            new SignupUser.SignupCommand(
                dto.email(),
                dto.password(),
                dto.fullName(),
                dto.phone(),
                dto.address(),
                dto.country(),
                dto.dateOfBirth()));

    boolean isTestProfile =
        environment.getActiveProfiles() != null
            && java.util.Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("test"));
    boolean exposeOtp = isTestProfile || exposeOtpResponse;

    Map<String, Object> body =
        exposeOtp
            ? Map.of(
                "accountId", result.accountId(),
                "verificationId", result.verificationId(),
                "otp", result.otp())
            : Map.of("accountId", result.accountId(), "verificationId", result.verificationId());

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
  }

  @PostMapping("/signup/confirm")
  public SignupUser.ConfirmResult confirm(@Valid @RequestBody ConfirmDto dto) {
    return signupUser.confirm(
        new SignupUser.ConfirmCommand(
            java.util.UUID.fromString(dto.accountId()), dto.otp().trim()));
  }
}
