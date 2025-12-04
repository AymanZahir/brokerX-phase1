package com.brokerx.application.support;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final Logger log = LoggerFactory.getLogger(JwtService.class);

  private final SecretKey secretKey;
  private final Clock clock;
  private final long expirationSeconds;

  public JwtService(
      @Value("${brokerx.security.jwt-secret}") String secret,
      @Value("${brokerx.security.jwt-expiration-seconds:3600}") long expirationSeconds,
      Clock clock) {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
    }
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.clock = clock;
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(UUID accountId) {
    Instant now = clock.instant();
    Instant expiresAt = now.plusSeconds(expirationSeconds);
    return Jwts.builder()
        .setSubject(accountId.toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiresAt))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public Optional<TokenClaims> parse(String token) {
    try {
      Jws<Claims> parsed =
          Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      Claims claims = parsed.getBody();
      return Optional.of(new TokenClaims(UUID.fromString(claims.getSubject())));
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return Optional.empty();
    }
  }

  public record TokenClaims(UUID accountId) {}
}
