package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login payload. {@code toString()} is overridden to redact {@code password} so an accidental log
 * of the DTO never leaks credentials.
 */
public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {

  @Override
  public String toString() {
    return "LoginRequest{email=" + email + ", password=***}";
  }
}
