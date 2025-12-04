package com.brokerx.adapters.web.support;

import com.brokerx.application.support.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long start = System.currentTimeMillis();
    String traceId = resolveTraceId(request);
    TraceContext.set(traceId);
    MDC.put("traceId", traceId);
    response.setHeader(TRACE_ID_HEADER, traceId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info(
          "traceId={} method={} path={} status={} durationMs={}",
          traceId,
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration);
      TraceContext.clear();
      MDC.remove("traceId");
    }
  }

  private String resolveTraceId(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
        .filter(h -> !h.isBlank())
        .orElseGet(() -> UUID.randomUUID().toString());
  }
}
