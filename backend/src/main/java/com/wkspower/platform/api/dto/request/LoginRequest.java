package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login payload. {@code toString()} is overridden to redact {@code password} and partially mask
 * {@code email} (PII) so an accidental log of the DTO never leaks credentials or identifies the
 * account precisely.
 */
public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {

  @Override
  public String toString() {
    return "LoginRequest{email=" + maskEmail(email) + ", password=***}";
  }

  private static String maskEmail(String value) {
    if (value == null || value.isBlank()) {
      return "***";
    }
    int at = value.indexOf('@');
    if (at <= 0) {
      return "***";
    }
    String local = value.substring(0, at);
    String domain = value.substring(at);
    return local.charAt(0) + "***" + domain;
  }
}
