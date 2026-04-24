package com.wkspower.platform.domain.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Single error entry inside a multi-error aggregate (see {@link WksConfigException}). Shape is the
 * wire contract for {@code error.errors[]} — {@code field} and {@code line} are optional (omitted
 * via {@link JsonInclude.Include#NON_NULL} when absent).
 *
 * <p>Placed in {@code domain/exception/} rather than {@code api/dto/response/} because domain
 * producers ({@code WksConfigException}) need to reference it and the hexagonal layering rule in
 * {@code ArchitectureTest} forbids domain→api imports. The transport ({@link
 * com.wkspower.platform.api.dto.ErrorPayload}) reads it from here. The Jackson annotation is a
 * permitted dependency — domain is forbidden from importing Spring / JPA / CIB seven only.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String code, String message, String field, Integer line) {

  public static ErrorDetail of(String code, String message) {
    return new ErrorDetail(code, message, null, null);
  }

  public static ErrorDetail ofField(String code, String message, String field) {
    return new ErrorDetail(code, message, field, null);
  }
}
