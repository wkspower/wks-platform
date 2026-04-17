package com.wkspower.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Standard WKS API envelope. Every REST response — success or error — has this shape.
 *
 * <pre>
 * Success: { "data": { ... }, "meta": { ... } }
 * Error:   { "error": { "code": "...", "message": "...", "field": "..." }, "meta": { ... } }
 * </pre>
 *
 * <p>{@code NON_NULL} omits absent fields: success responses have no {@code "error"} key; error
 * responses have no {@code "data"} key. This keeps the wire format clean and matches the spec
 * envelope exactly.
 *
 * @param data the response payload on success, {@code null} on error
 * @param error error details on failure, {@code null} on success
 * @param meta optional metadata (pagination, timings) — always serialised, never {@code null}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, ErrorPayload error, Map<String, Object> meta) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null, Map.of());
  }

  public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
    return new ApiResponse<>(data, null, meta);
  }

  public static <T> ApiResponse<T> error(ErrorPayload error) {
    return new ApiResponse<>(null, error, Map.of());
  }
}
