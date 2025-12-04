package com.brokerx.adapters.web.support;

import com.brokerx.application.support.TraceContext;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public record ApiError(String code, String message, Instant timestamp, String traceId) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("Validation failed");
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, ex);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage(), ex);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), ex.getMessage(), ex);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", ex);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, String code, String message, Exception exception) {
    String traceId = TraceContext.currentTraceId().orElse(null);
    if (status.is5xxServerError()) {
      log.error("traceId={} errorCode={} message={}", traceId, code, message, exception);
    } else {
      log.warn("traceId={} errorCode={} message={}", traceId, code, message);
    }
    ApiError payload = new ApiError(code, message, Instant.now(), traceId);
    return ResponseEntity.status(status).body(payload);
  }
}
