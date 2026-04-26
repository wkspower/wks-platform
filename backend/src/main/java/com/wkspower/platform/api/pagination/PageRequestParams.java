package com.wkspower.platform.api.pagination;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Shared query-string contract for list endpoints: {@code ?page=0&size=20&sort=field[,dir]}.
 *
 * <p>Defaults — {@code page=0}, {@code size=20}, no {@code sort}. {@code page} is zero-based
 * (Spring Data aligned and matches the PRD wire format exactly). {@code size} is clamped to {@code
 * [1, 100]} — values above {@code 100} are silently reduced (AC4); values below {@code 1} raise
 * {@code WKS-API-003} at 400.
 *
 * <p>The {@link ParameterObject} annotation hints springdoc to flatten the fields into discrete
 * query parameters in the generated OpenAPI spec. Controllers bind via {@link #of(Integer, Integer,
 * List)} from explicit {@code @RequestParam} method arguments (records don't participate in
 * {@code @RequestParam} default-value resolution, so binding stays at the controller layer).
 */
@ParameterObject
public record PageRequestParams(int page, int size, List<String> sort) {

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final int MAX_SIZE = 100;

  /**
   * Upper bound on {@code page} — guards against deep-offset scans. At {@link #MAX_SIZE} rows per
   * page, this caps OFFSET at {@code 10_000 * 100 = 1_000_000}, which is already beyond any
   * realistic UI pagination need. Clients hitting the cap should filter or switch to cursor-based
   * traversal rather than paginating deeper.
   */
  public static final int MAX_PAGE = 10_000;

  /**
   * Construct with defaults applied when caller passes nulls (typical from {@code @RequestParam}).
   */
  public static PageRequestParams of(Integer page, Integer size, List<String> sort) {
    return new PageRequestParams(
        page == null ? DEFAULT_PAGE : page,
        size == null ? DEFAULT_SIZE : size,
        sort == null ? List.of() : sort);
  }

  /**
   * Parse raw {@code sort} tokens into {@link SortSpec} instances. Returns an empty list if none.
   */
  public List<SortSpec> parsedSort() {
    if (sort == null || sort.isEmpty()) {
      return List.of();
    }
    List<SortSpec> specs = new ArrayList<>(sort.size());
    for (String token : sort) {
      specs.add(SortSpec.parse(token));
    }
    return List.copyOf(specs);
  }

  /**
   * Build a Spring Data {@link Pageable} after validating and whitelisting. {@code size} is clamped
   * to {@link #MAX_SIZE}; negative {@code size} and negative {@code page} raise {@link
   * ErrorCode#WKS_API_003}. Each sort property is checked against {@code whitelist}.
   */
  public Pageable toPageable(SortWhitelist whitelist) {
    if (page < 0) {
      throw new WksValidationException(ErrorCode.WKS_API_003, "page must be >= 0", "page");
    }
    if (page > MAX_PAGE) {
      throw new WksValidationException(
          ErrorCode.WKS_API_003, "page must be <= " + MAX_PAGE, "page");
    }
    if (size < 1) {
      throw new WksValidationException(ErrorCode.WKS_API_003, "size must be >= 1", "size");
    }
    int effectiveSize = Math.min(size, MAX_SIZE);

    List<SortSpec> specs = parsedSort();
    if (specs.isEmpty()) {
      return PageRequest.of(page, effectiveSize);
    }
    // Story 2.5 AC11 #1 — last-wins dedup. `?sort=name,asc&sort=name,desc` should not emit two
    // ORDER BY clauses on the same column. Insertion order is preserved for unique properties; on
    // a duplicate property the entry rewrites in place (LinkedHashMap.put semantics: key keeps its
    // original slot, value updates).
    Map<String, SortSpec> deduped = new LinkedHashMap<>();
    for (SortSpec spec : specs) {
      deduped.put(spec.property(), spec);
    }
    List<Sort.Order> orders = new ArrayList<>(deduped.size());
    for (SortSpec spec : deduped.values()) {
      whitelist.assertAllowed(spec.property());
      orders.add(new Sort.Order(spec.direction().toSpring(), spec.property()));
    }
    return PageRequest.of(page, effectiveSize, Sort.by(orders));
  }
}
