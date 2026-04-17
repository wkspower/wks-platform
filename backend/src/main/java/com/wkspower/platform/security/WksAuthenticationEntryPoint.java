package com.wkspower.platform.security;

import com.wkspower.platform.domain.exception.WksAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Converts the Spring Security "no authentication" state into the WKS error envelope with status
 * 401 and code {@code WKS-API-401} — ensuring protected endpoints behave identically to our {@link
 * GlobalExceptionHandler} handlers.
 */
public class WksAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String BODY =
      "{\"error\":{\"code\":\""
          + WksAuthenticationException.CODE
          + "\",\"message\":\""
          + WksAuthenticationException.DEFAULT_MESSAGE
          + "\",\"field\":null},\"meta\":{}}";

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(BODY);
  }
}
