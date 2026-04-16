package com.wkspower.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Standard WKS API envelope. Every REST response — success or error — has this shape.
 *
 * <pre>
 * { "data": { ... } | null, "error": { "code": "...", "message": "...", "field": "..." } | null, "meta": { ... } }
 * </pre>
 *
 * @param data the response payload on success, {@code null} on error
 * @param error error details on failure, {@code null} on success
 * @param meta optional metadata (pagination, timings) — always serialised, never {@code null}
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
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
