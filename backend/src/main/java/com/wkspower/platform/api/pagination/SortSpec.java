package com.wkspower.platform.api.pagination;

import java.util.Locale;
import org.springframework.data.domain.Sort;

/**
 * Parsed representation of one {@code sort} query-string segment. {@code property} is the
 * dot-separated field name (e.g. {@code updatedAt}); {@code direction} defaults to {@link
 * Direction#ASC} when the client omits it.
 *
 * <p>Wire format is {@code property[,direction]} — e.g. {@code sort=updatedAt,desc}. Unknown
 * directions raise {@code WKS-API-005}; unknown properties raise {@code WKS-API-004} (validated at
 * the controller layer against a resource-specific allow-list, never against Hibernate).
 */
public record SortSpec(String property, Direction direction) {

  public enum Direction {
    ASC,
    DESC;

    public Sort.Direction toSpring() {
      return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
  }

  /**
   * Parse a single {@code property[,direction]} token. Whitespace is stripped around both halves.
   *
   * @throws com.wkspower.platform.domain.exception.WksValidationException with code {@code
   *     WKS-API-005} if the direction is not {@code asc} or {@code desc}.
   */
  public static SortSpec parse(String token) {
    if (token == null || token.isBlank()) {
      throw new com.wkspower.platform.domain.exception.WksValidationException(
          com.wkspower.platform.domain.exception.ErrorCode.WKS_API_004,
          "Sort parameter is blank",
          "sort");
    }
    String[] parts = token.split(",", 2);
    String property = parts[0].strip();
    if (property.isEmpty()) {
      throw new com.wkspower.platform.domain.exception.WksValidationException(
          com.wkspower.platform.domain.exception.ErrorCode.WKS_API_004,
          "Sort property is blank",
          "sort");
    }
    Direction direction = Direction.ASC;
    if (parts.length == 2) {
      String raw = parts[1].strip().toLowerCase(Locale.ROOT);
      direction =
          switch (raw) {
            case "", "asc" -> Direction.ASC;
            case "desc" -> Direction.DESC;
            default ->
                throw new com.wkspower.platform.domain.exception.WksValidationException(
                    com.wkspower.platform.domain.exception.ErrorCode.WKS_API_005,
                    "Unknown sort direction: " + parts[1],
                    "sort");
          };
    }
    return new SortSpec(property, direction);
  }
}
