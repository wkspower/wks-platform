package com.wkspower.platform.api.pagination;

import com.wkspower.platform.api.dto.ApiResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Utility that turns a Spring Data {@link Page} into the WKS envelope. Use this — do NOT hand-build
 * {@code Map.of("total", ..., "page", ..., "size", ...)} per controller — so the envelope stays
 * consistent and a single place owns the {@code page}, {@code size}, {@code total} key names.
 */
public final class PageMetaBuilder {

  private PageMetaBuilder() {}

  /**
   * Metadata map only — use this when you already have the mapped payload.
   *
   * <p>Uses {@link LinkedHashMap} (not {@link Map#of}) so the JSON wire order is stable: {@code
   * total}, {@code page}, {@code size}. {@code Map.of} does not specify iteration order, which
   * leaves Jackson free to emit keys in an order that varies between JVM versions — breaking
   * snapshot-style tests on the frontend.
   */
  public static Map<String, Object> meta(Page<?> page) {
    Map<String, Object> meta = new LinkedHashMap<>(3);
    meta.put("total", page.getTotalElements());
    meta.put("page", page.getNumber());
    meta.put("size", page.getSize());
    return meta;
  }

  /**
   * Full {@link ApiResponse} — maps each entity with {@code toDto} and wraps in the envelope with
   * pagination meta. This is the ONLY allowed path for building paged responses (see Dev Notes).
   */
  public static <T, D> ApiResponse<List<D>> paged(Page<T> page, Function<T, D> toDto) {
    List<D> content = page.getContent().stream().map(toDto).toList();
    return ApiResponse.success(content, meta(page));
  }
}
