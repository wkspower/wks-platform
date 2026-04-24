package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;

/**
 * Assembles an {@link ErrorDetail} for the validator, looking the line up via {@link YamlLineIndex}
 * so every call site produces the canonical {@code (code, message, field, line)} tuple. Reviewers
 * grep for literal {@code "WKS-CFG-"} in call sites — using this helper keeps codes referenced by
 * the enum wire string only.
 */
final class ErrorBuilder {

  private final YamlLineIndex lines;

  ErrorBuilder(YamlLineIndex lines) {
    this.lines = lines;
  }

  ErrorDetail error(ErrorCode code, String message, String field) {
    Integer line = lines.lineOfOrNearest(field).orElse(null);
    return ErrorDetail.ofFieldLine(code.wire(), message, field, line);
  }

  ErrorDetail errorAt(ErrorCode code, String message, String field, String lineLookupField) {
    Integer line = lines.lineOfOrNearest(lineLookupField).orElse(null);
    return ErrorDetail.ofFieldLine(code.wire(), message, field, line);
  }

  ErrorDetail errorNoLine(ErrorCode code, String message, String field) {
    return ErrorDetail.ofField(code.wire(), message, field);
  }
}
