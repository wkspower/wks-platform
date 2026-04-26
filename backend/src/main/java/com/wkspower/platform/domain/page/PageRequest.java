package com.wkspower.platform.domain.page;

import java.util.List;
import java.util.Objects;

/**
 * Domain-side pagination request. Mirrors Spring Data's {@code Pageable} shape but lives in the
 * domain layer so ports don't import Spring. The api-layer {@code PageRequestParams} (Story 1.4) is
 * mapped to this record by controllers.
 *
 * @param page zero-based page index
 * @param size number of rows per page
 * @param sort ordered list of sort directives; first element is the primary key
 */
public record PageRequest(int page, int size, List<SortOrder> sort) {

  public PageRequest {
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size < 1) {
      throw new IllegalArgumentException("size must be >= 1");
    }
    sort = List.copyOf(Objects.requireNonNull(sort, "sort"));
  }

  public static PageRequest of(int page, int size, List<SortOrder> sort) {
    return new PageRequest(page, size, sort);
  }
}
