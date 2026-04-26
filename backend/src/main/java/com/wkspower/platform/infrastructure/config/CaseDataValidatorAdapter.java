package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.CaseDataValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Adapter that bridges {@link JsonSchemaGenerator} (domain-config-driven schema construction) and
 * the networknt JSON Schema validator. Returns the full list of violations — never short-circuits
 * (collect-all invariant). The Jackson {@link ObjectMapper} comes from Story 1.4's {@code
 * JacksonConfig}; the schema factory is built once per JVM (Draft 2020-12).
 */
@Component
class CaseDataValidatorAdapter implements CaseDataValidator {

  private static final JsonSchemaFactory SCHEMA_FACTORY =
      JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

  private final JsonSchemaGenerator schemaGenerator;
  private final ObjectMapper objectMapper;

  CaseDataValidatorAdapter(JsonSchemaGenerator schemaGenerator, ObjectMapper objectMapper) {
    this.schemaGenerator = schemaGenerator;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
    JsonNode schemaNode = schemaGenerator.generate(caseType);
    JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
    JsonNode payload = objectMapper.valueToTree(data == null ? Map.of() : data);
    Set<ValidationMessage> messages = schema.validate(payload);

    List<ErrorDetail> errors = new ArrayList<>();
    for (ValidationMessage m : messages) {
      String field = pointerToField(m.getInstanceLocation().toString());
      errors.add(ErrorDetail.ofField(ErrorCode.WKS_API_001.wire(), m.getMessage(), field));
    }
    return List.copyOf(errors);
  }

  private static String pointerToField(String pointer) {
    if (pointer == null || pointer.isEmpty() || "$".equals(pointer)) {
      return null;
    }
    // networknt 1.5 uses JsonPath-style locations like "$.applicant_name". Strip the prefix.
    if (pointer.startsWith("$.")) {
      return pointer.substring(2);
    }
    if (pointer.startsWith("/")) {
      return pointer.substring(1);
    }
    return pointer;
  }
}
