package com.wkspower.platform.audit;

import com.wkspower.platform.domain.model.AuditSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Story 9-3 — serializes the sealed {@link AuditSource} interface to/from the two-column DB shape
 * ({@code source_type VARCHAR}, {@code source_payload JSON}). AC4 round-trip contract: every
 * variant reconstructs identically after a write/read cycle.
 *
 * <p>{@code source_type} values are stable wire strings (uppercase, snake_case) matching the sealed
 * permit list — per {@code feedback_error_codes_are_wire_contract} philosophy, these must NEVER be
 * renamed. They are independent of {@link AuditSource#toString()} which renders the legacy
 * bare-wire vocabulary ({@code "manual"}, {@code "wks-auto-rule"}, {@code "backend(...)"}, {@code
 * "execution(unmapped:...)"}) for SI-runbook compatibility.
 *
 * <p>{@code source_payload} is variant-shaped JSON:
 *
 * <ul>
 *   <li>{@code USER}: {@code {"actorId": "<uuid>"}}
 *   <li>{@code AUTO_RULE}: {@code {"ruleId": "<str>"}}
 *   <li>{@code BACKEND}: {@code {"adapterName": "<str>"}}
 *   <li>{@code EXECUTION_UNMAPPED}: {@code {"originAdapter": "<str>"}}
 * </ul>
 *
 * <p>Hibernate's {@code @JdbcTypeCode(SqlTypes.JSON)} on {@code Map<String, Object>} handles the
 * Jackson serialization automatically — this class deals only with the in-memory Map shape, never
 * raw JSON strings.
 *
 * <p>Reading is exhaustive on the four discriminator strings; an unrecognised {@code source_type}
 * throws {@link IllegalArgumentException} (loud failure preferable to silent default per {@code
 * feedback_silent_fallback_defeats_pin}).
 */
public final class AuditEventMapper {

  static final String SOURCE_USER = "USER";
  static final String SOURCE_AUTO_RULE = "AUTO_RULE";
  static final String SOURCE_BACKEND = "BACKEND";
  static final String SOURCE_EXECUTION_UNMAPPED = "EXECUTION_UNMAPPED";

  private AuditEventMapper() {
    // utility
  }

  /** Map an {@link AuditSource} to its persistence discriminator string. */
  public static String sourceType(AuditSource source) {
    return switch (source) {
      case AuditSource.User u -> SOURCE_USER;
      case AuditSource.AutoRule a -> SOURCE_AUTO_RULE;
      case AuditSource.Backend b -> SOURCE_BACKEND;
      case AuditSource.ExecutionUnmapped e -> SOURCE_EXECUTION_UNMAPPED;
    };
  }

  /** Map an {@link AuditSource} to its persistence payload map. */
  public static Map<String, Object> sourcePayload(AuditSource source) {
    // LinkedHashMap so the serialized JSON byte-shape is deterministic across runs — relevant if a
    // future variant grows beyond a single key.
    Map<String, Object> payload = new LinkedHashMap<>();
    switch (source) {
      case AuditSource.User u -> payload.put("actorId", u.actorId().toString());
      case AuditSource.AutoRule a -> payload.put("ruleId", a.ruleId());
      case AuditSource.Backend b -> payload.put("adapterName", b.adapterName());
      case AuditSource.ExecutionUnmapped e -> payload.put("originAdapter", e.originAdapter());
    }
    return payload;
  }

  /**
   * Reconstruct an {@link AuditSource} from the (type, payload) DB tuple. Throws on unknown
   * discriminators or missing required keys.
   */
  public static AuditSource fromColumns(String sourceType, Map<String, Object> payload) {
    if (sourceType == null) {
      throw new IllegalArgumentException("source_type cannot be null");
    }
    if (payload == null) {
      throw new IllegalArgumentException("source_payload cannot be null");
    }
    return switch (sourceType) {
      case SOURCE_USER -> new AuditSource.User(UUID.fromString(requireString(payload, "actorId")));
      case SOURCE_AUTO_RULE -> new AuditSource.AutoRule(requireString(payload, "ruleId"));
      case SOURCE_BACKEND -> new AuditSource.Backend(requireString(payload, "adapterName"));
      case SOURCE_EXECUTION_UNMAPPED ->
          new AuditSource.ExecutionUnmapped(requireString(payload, "originAdapter"));
      default ->
          throw new IllegalArgumentException(
              "Unknown source_type: '"
                  + sourceType
                  + "' (expected one of USER, AUTO_RULE, BACKEND,"
                  + " EXECUTION_UNMAPPED)");
    };
  }

  private static String requireString(Map<String, Object> payload, String key) {
    Object value = payload.get(key);
    if (value == null) {
      throw new IllegalArgumentException(
          "source_payload missing required key '" + key + "': " + payload);
    }
    return value.toString();
  }
}
