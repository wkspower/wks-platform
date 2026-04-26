package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Map;

/**
 * Outbound port that validates a case's dynamic {@code data} map against the JSON Schema derived
 * from a case-type's field definitions. Implementations live in {@code infrastructure/config/}
 * (where the schema generator and the third-party schema-validator library are wired). Returns the
 * full list of violations — never short-circuits on first error (collect-all invariant from Story
 * 2.1 / 2.2).
 */
public interface CaseDataValidator {

  /**
   * Validate {@code data} against the JSON Schema generated for {@code caseType}. An empty list
   * means valid; otherwise each entry carries a {@code WKS-API-001}-style code, message, and (where
   * applicable) the offending field id.
   */
  List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data);
}
