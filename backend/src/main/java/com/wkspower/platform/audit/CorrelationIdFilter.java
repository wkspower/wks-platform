package com.wkspower.platform.audit;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Generates a UUID correlation ID per HTTP request, publishes it to SLF4J MDC for structured
 * logging, and echoes it back as the {@code X-Correlation-Id} response header. The MDC entry is
 * always cleared in a {@code finally} block to prevent leak across thread-pool reuse.
 */
@Component
public class CorrelationIdFilter implements Filter {

  static final String MDC_KEY = "correlationId";
  static final String HEADER_NAME = "X-Correlation-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String correlationId = UUID.randomUUID().toString();
    MDC.put(MDC_KEY, correlationId);
    try {
      if (response instanceof HttpServletResponse httpResponse) {
        httpResponse.setHeader(HEADER_NAME, correlationId);
      }
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
