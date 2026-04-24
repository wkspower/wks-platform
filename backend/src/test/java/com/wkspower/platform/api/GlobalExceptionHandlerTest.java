package com.wkspower.platform.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.domain.exception.WksAuthorizationException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Unit test for the error codes produced by {@link GlobalExceptionHandler}. These paths are not
 * reachable through {@link com.wkspower.platform.api.controller.AuthController} in Phase 0, so a
 * direct handler unit test prevents silent drift on {@code WKS-API-403}, {@code WKS-API-002}, and
 * {@code WKS-RTM-500}.
 */
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @SuppressWarnings("unchecked")
  void authorizationExceptionMapsTo403WithWksApi403() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleAuthz(new WksAuthorizationException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("WKS-API-403");
    assertThat(response.getBody().data()).isNull();
  }

  @Test
  void malformedJsonMapsTo400WithWksApi002() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException(
            "broken json", new MockHttpInputMessage("{".getBytes()));
    ResponseEntity<Object> response =
        handler.handleHttpMessageNotReadable(
            ex,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            new ServletWebRequest(new MockHttpServletRequest()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ApiResponse<?> body = (ApiResponse<?>) response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.error().code()).isEqualTo("WKS-API-002");
  }

  @Test
  @SuppressWarnings("unchecked")
  void unexpectedExceptionMapsTo500WithWksRtm500AndHidesStack() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleUnexpected(new RuntimeException("boom — internal secret detail"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("WKS-RTM-500");
    // Body message must not leak the internal exception detail.
    assertThat(response.getBody().error().message()).doesNotContain("boom");
  }

  @Test
  @SuppressWarnings("unchecked")
  void domainValidationExceptionPopulatesField() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleDomainValidation(new WksValidationException("bad value", "email"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("WKS-API-001");
    assertThat(response.getBody().error().field()).isEqualTo("email");
  }

  @Test
  @SuppressWarnings("unchecked")
  void notFoundExceptionMapsTo404WithWksApi404() {
    ResponseEntity<ApiResponse<Void>> response =
        handler.handleNotFound(new WksNotFoundException("user xyz not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("WKS-API-404");
    assertThat(response.getBody().error().message()).isEqualTo("user xyz not found");
    assertThat(response.getBody().error().field()).isNull();
    assertThat(response.getBody().data()).isNull();
  }
}
