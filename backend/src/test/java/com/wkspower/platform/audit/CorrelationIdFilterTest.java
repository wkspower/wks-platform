package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void setsCorrelationIdOnMdcDuringChainAndClearsAfter() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    doAnswer(
            inv -> {
              mdcDuringChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
              return null;
            })
        .when(chain)
        .doFilter(any(), any());

    filter.doFilter(request, response, chain);

    assertThat(mdcDuringChain.get()).as("MDC populated inside chain").isNotBlank();
    assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).as("MDC cleared after chain returns").isNull();
  }

  @Test
  void generatesUuidFormatCorrelationId() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    AtomicReference<String> captured = new AtomicReference<>();
    doAnswer(
            inv -> {
              captured.set(MDC.get(CorrelationIdFilter.MDC_KEY));
              return null;
            })
        .when(chain)
        .doFilter(any(), any());

    filter.doFilter(request, response, chain);

    // Throws IllegalArgumentException if not UUID-shaped.
    assertThat(UUID.fromString(captured.get())).isNotNull();
  }

  @Test
  void writesCorrelationIdToResponseHeader() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(response).setHeader(eq(CorrelationIdFilter.HEADER_NAME), anyString());
  }
}
