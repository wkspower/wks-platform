package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Reads a case-type YAML file and returns a {@link RawReadResult} containing either the parsed
 * transport record + line index, or a single {@code WKS-CFG-099} error detail. Never throws for
 * parse failures — the startup loader and (future) admin deploy controller both treat the result
 * identically to validator output.
 *
 * <p>A dedicated {@link YAMLMapper} is used so the strict JSON {@code ObjectMapper} (which enforces
 * {@code FAIL_ON_UNKNOWN_PROPERTIES}) is not coupled to YAML forward-compatibility.
 */
@Component
public class CaseTypeYamlLoader {

  /** Hard cap on incoming YAML bytes — guards against pathological inputs. */
  static final int MAX_YAML_BYTES = 1_048_576;

  private final YAMLMapper mapper;

  public CaseTypeYamlLoader() {
    YAMLMapper m =
        YAMLMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule())
            // Story 4.3.1 AC8 — enable strict unknown-property failure at the mapper level. The
            // top-level transport record ({@link RawCaseTypeConfig}) keeps {@code
            // @JsonIgnoreProperties(ignoreUnknown = true)} so future case-type-level keys ship
            // forward-compat; mapping subtree records (RawAttachment, RawRoutingBlock,
            // RawUserTaskMapping, RawEventMappings, RawEndEventMapping, RawSignalMapping,
            // RawPropertyEmissionRule, RawEmits) drop the annotation so unknown keys surface as
            // {@code WKS-MAP-008}. Scoped — does NOT flip the global Jackson ObjectMapper.
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .build();
    m.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    // Forbid number → string coercion (e.g. SELECT option `value: 42`).
    m.coercionConfigFor(LogicalType.Textual)
        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
    this.mapper = m;
  }

  /**
   * Load from a filesystem path. The path is captured verbatim in {@link RawReadResult#source()}
   * for error reporting.
   */
  public RawReadResult read(Path file) {
    if (!Files.isRegularFile(file)) {
      return RawReadResult.error(
          file.toString(), ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "File not found: " + file));
    }
    try {
      long size = Files.size(file);
      if (size > MAX_YAML_BYTES) {
        return RawReadResult.error(
            file.toString(),
            ErrorDetail.of(
                ErrorCode.WKS_CFG_099.wire(),
                "YAML file size " + size + " bytes exceeds maximum of " + MAX_YAML_BYTES));
      }
    } catch (IOException e) {
      return RawReadResult.error(
          file.toString(),
          ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "I/O failure: " + e.getMessage()));
    }
    try (InputStream in = Files.newInputStream(file)) {
      byte[] bytes = in.readAllBytes();
      return readBytes(file.toString(), bytes);
    } catch (IOException e) {
      return RawReadResult.error(
          file.toString(),
          ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "I/O failure: " + e.getMessage()));
    }
  }

  /** Load from raw bytes — used by tests and the (future) admin deploy endpoint. */
  public RawReadResult readBytes(String source, byte[] bytes) {
    if (bytes == null) {
      return RawReadResult.error(
          source, ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "empty content"));
    }
    if (bytes.length > MAX_YAML_BYTES) {
      return RawReadResult.error(
          source,
          ErrorDetail.of(
              ErrorCode.WKS_CFG_099.wire(),
              "YAML content size " + bytes.length + " bytes exceeds maximum of " + MAX_YAML_BYTES));
    }
    // Pre-scan for YAML alias markers — anchors/aliases are not supported.
    if (containsYamlAlias(bytes)) {
      return RawReadResult.error(
          source,
          ErrorDetail.of(
              ErrorCode.WKS_CFG_099.wire(),
              "anchors and aliases are not supported in case-type YAML"));
    }
    RawCaseTypeConfig raw;
    try (MappingIterator<RawCaseTypeConfig> it =
        mapper.readerFor(RawCaseTypeConfig.class).readValues(bytes)) {
      if (!it.hasNextValue()) {
        return RawReadResult.error(
            source, ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "YAML document is empty"));
      }
      raw = it.nextValue();
      if (it.hasNextValue()) {
        return RawReadResult.error(
            source,
            ErrorDetail.of(
                ErrorCode.WKS_CFG_099.wire(),
                "case-type files must contain exactly one YAML document"));
      }
    } catch (UnrecognizedPropertyException upe) {
      // Story 4.3.1 AC8 — unknown YAML key (typo). Surface with a Mapping-namespaced wire code so
      // operators can grep WKS-MAP-008 and locate the offending key + JSON pointer.
      String pointer = jsonPointerOf(upe);
      String key = upe.getPropertyName() == null ? "<unknown>" : upe.getPropertyName();
      return RawReadResult.error(
          source,
          ErrorDetail.ofField(
              ErrorCode.WKS_MAP_008.wire(),
              "Unknown YAML key '" + key + "' in " + source,
              pointer));
    } catch (IOException e) {
      // Story 4.3.1 AC9 — STRICT_DUPLICATE_DETECTION raises a JsonMappingException whose message
      // contains "Duplicate field". Surface as WKS-MAP-009 ONLY when the duplicate falls inside
      // the mapping subtree (attachments[]) — top-level duplicates remain WKS-CFG-099 to preserve
      // the wire contract pinned by ConfigValidatorTest.wksCfg099_duplicateTopLevelKey.
      String msg = e.getMessage();
      if (msg != null && msg.contains("Duplicate field")) {
        boolean inMappingSubtree = false;
        if (e instanceof com.fasterxml.jackson.databind.JsonMappingException jme) {
          for (var ref : jme.getPath()) {
            if (ref.getFieldName() != null && ref.getFieldName().equals("attachments")) {
              inMappingSubtree = true;
              break;
            }
          }
        }
        if (inMappingSubtree) {
          return RawReadResult.error(
              source,
              ErrorDetail.of(
                  ErrorCode.WKS_MAP_009.wire(), "Duplicate YAML key in " + source + ": " + msg));
        }
      }
      return RawReadResult.error(
          source, ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "YAML parse error: " + msg));
    }
    if (raw == null) {
      return RawReadResult.error(
          source, ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "YAML document is empty"));
    }
    YamlLineIndex index;
    try {
      index = YamlLineIndex.of(new java.io.ByteArrayInputStream(bytes));
    } catch (IOException e) {
      // Line capture failure is not fatal — fall back to empty index.
      index = YamlLineIndex.empty();
    }
    return RawReadResult.ok(source, raw, index);
  }

  /**
   * Build a JSON-pointer-style locator from a Jackson {@link UnrecognizedPropertyException}'s
   * property path. {@code "/attachments/0/map/events/signl"} when the path traverses {@code
   * attachments[0].map.events.signl}.
   */
  private static String jsonPointerOf(UnrecognizedPropertyException upe) {
    StringBuilder sb = new StringBuilder();
    if (upe.getPath() != null) {
      for (var ref : upe.getPath()) {
        if (ref.getFieldName() != null) {
          sb.append('/').append(ref.getFieldName());
        } else if (ref.getIndex() >= 0) {
          sb.append('/').append(ref.getIndex());
        }
      }
    }
    if (upe.getPropertyName() != null) {
      sb.append('/').append(upe.getPropertyName());
    }
    return sb.length() == 0 ? "/" : sb.toString();
  }

  /**
   * Best-effort line-oriented scan for YAML anchor (&name) / alias (*name) sigils outside string
   * literals. False positives on `&` inside quoted strings are tolerated because YAML in case-type
   * configs has no legitimate use for embedded ampersands or asterisks at token-start positions.
   */
  private static boolean containsYamlAlias(byte[] bytes) {
    String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    for (String rawLine : text.split("\\r?\\n", -1)) {
      String line = rawLine;
      int hash = line.indexOf('#');
      if (hash >= 0) {
        line = line.substring(0, hash);
      }
      // Look for ` &word` or ` *word` tokens — anchors must be preceded by whitespace or appear at
      // map/array value position. Conservative match: whitespace then & or *.
      int n = line.length();
      for (int i = 0; i < n; i++) {
        char c = line.charAt(i);
        if (c != '&' && c != '*') {
          continue;
        }
        if (i > 0) {
          char prev = line.charAt(i - 1);
          if (prev != ' ' && prev != '\t') {
            continue;
          }
        }
        if (i + 1 < n) {
          char next = line.charAt(i + 1);
          if (Character.isLetterOrDigit(next) || next == '_') {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Result of a read pass — either a parsed raw config + line index, or catastrophic errors ({@code
   * WKS-CFG-099}). Success is not the same as "validated": the validator has not yet run.
   */
  public record RawReadResult(
      String source, RawCaseTypeConfig raw, YamlLineIndex lines, List<ErrorDetail> errors) {

    public static RawReadResult ok(String source, RawCaseTypeConfig raw, YamlLineIndex lines) {
      return new RawReadResult(source, raw, lines, List.of());
    }

    public static RawReadResult error(String source, ErrorDetail error) {
      return new RawReadResult(source, null, YamlLineIndex.empty(), List.of(error));
    }

    public boolean isParsed() {
      return raw != null;
    }
  }
}
