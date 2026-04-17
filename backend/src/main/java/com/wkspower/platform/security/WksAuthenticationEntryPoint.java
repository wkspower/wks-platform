package com.wkspower.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.exception.WksAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Converts the Spring Security "no authentication" state into the WKS error envelope with status
 * 401 and code {@code WKS-API-401}. Serialization goes through the shared {@link ObjectMapper} so
 * JSON escaping is correct even if the code/message ever carry special characters — but we build
 * the envelope from primitives locally rather than depending on {@code api.dto.*}, which the
 * hexagonal-layering ArchUnit rule forbids {@code security} to reach into.
 */
public class WksAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public WksAuthenticationEntryPoint() {
    this(new ObjectMapper());
  }

  @Autowired
  public WksAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    Map<String, Object> error = new LinkedHashMap<>();
    error.put("code", WksAuthenticationException.CODE);
    error.put("message", WksAuthenticationException.DEFAULT_MESSAGE);
    error.put("field", null);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", error);
    body.put("meta", Map.of());

    objectMapper.writeValue(response.getWriter(), body);
  }
}
