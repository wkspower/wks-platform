package com.wkspower.platform.api.pagination;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationException;
import java.util.Set;

/**
 * Per-resource whitelist of sortable property names. Controllers implement this (typically as a
 * constant {@code Set<String>}) and pass it to {@link PageRequestParams#toPageable(SortWhitelist)}
 * so unknown sort properties fail fast with {@link ErrorCode#WKS_API_004} rather than leaking into
 * Hibernate and emitting invalid SQL.
 */
public interface SortWhitelist {

  Set<String> allowedSortProperties();

  /**
   * Throws {@link WksValidationException} with code {@code WKS-API-004} if {@code property} is not
   * in {@link #allowedSortProperties()}.
   */
  default void assertAllowed(String property) {
    if (!allowedSortProperties().contains(property)) {
      throw new WksValidationException(
          ErrorCode.WKS_API_004, "Unknown sort property: " + property, "sort");
    }
  }
}
