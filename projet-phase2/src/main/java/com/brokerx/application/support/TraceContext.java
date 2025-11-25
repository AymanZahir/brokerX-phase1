package com.brokerx.application.support;

import java.util.Optional;

public final class TraceContext {

  private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

  private TraceContext() {}

  public static void set(String traceId) {
    TRACE_ID.set(traceId);
  }

  public static Optional<String> currentTraceId() {
    return Optional.ofNullable(TRACE_ID.get());
  }

  public static void clear() {
    TRACE_ID.remove();
  }
}
