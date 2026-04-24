package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
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

  private final YAMLMapper mapper;

  public CaseTypeYamlLoader() {
    this.mapper =
        YAMLMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build();
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
    RawCaseTypeConfig raw;
    try {
      raw = mapper.readValue(bytes, RawCaseTypeConfig.class);
    } catch (IOException e) {
      return RawReadResult.error(
          source,
          ErrorDetail.of(ErrorCode.WKS_CFG_099.wire(), "YAML parse error: " + e.getMessage()));
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
