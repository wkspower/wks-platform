package com.wkspower.platform.domain.page;

import java.util.List;
import java.util.Objects;

/**
 * Domain-side page envelope. The api-layer {@code ApiResponse.meta} is built from this via {@code
 * PageMetaBuilder}; ports return this type rather than Spring Data's {@code Page} so the domain
 * stays Spring-free.
 */
public record Page<T>(List<T> content, long total, int page, int size) {

  public Page {
    content = List.copyOf(Objects.requireNonNull(content, "content"));
    if (total < 0) {
      throw new IllegalArgumentException("total must be >= 0");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size < 1) {
      throw new IllegalArgumentException("size must be >= 1");
    }
  }

  public boolean hasNext() {
    return (long) (page + 1) * size < total;
  }
}
