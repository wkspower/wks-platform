package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Round-trip test: feed a YAML fixture with all seven field types through validator → generator →
 * {@code networknt} validator to prove the generated schema actually rejects a bad payload and
 * accepts a good one.
 */
class JsonSchemaGeneratorTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final ConfigValidator validator = new ConfigValidator();
  private final JsonSchemaGenerator generator = new JsonSchemaGenerator();
  private final ObjectMapper mapper = new ObjectMapper();

  private static final String ALL_TYPES_YAML =
      """
      id: everything
      displayName: Everything
      version: 1
      workflow:
        bpmn: x.bpmn
      fields:
        - id: applicant_name
          displayName: Applicant
          type: text
          required: true
        - id: loan_amount
          displayName: Amount
          type: number
          required: true
        - id: apply_date
          displayName: Applied
          type: date
        - id: product
          displayName: Product
          type: select
          options:
            - label: Salaried
              value: salaried
            - label: Self
              value: self
        - id: consent
          displayName: Consent
          type: checkbox
        - id: notes
          displayName: Notes
          type: textarea
        - id: kyc_doc
          displayName: KYC
          type: file
      statuses:
        - id: open
          displayName: Open
      listColumns: [applicant_name]
      roles:
        - name: officer
          permissions: [view]
      """;

  @Test
  void emitsKeysInLoadBearingOrder() {
    JsonNode schema = generate(ALL_TYPES_YAML);
    // Jackson ObjectNode preserves insertion order — matters for Story 2.5/2.7 snapshots.
    var fieldNames = schema.fieldNames();
    assertThat(fieldNames)
        .toIterable()
        .containsExactly("$schema", "type", "properties", "required", "additionalProperties");
  }

  @Test
  void validPayloadPasses() {
    JsonNode schema = generate(ALL_TYPES_YAML);
    JsonSchema jsonSchema = compile(schema);

    String payload =
        """
        {
          "applicant_name": "Priya",
          "loan_amount": 500000,
          "apply_date": "2026-04-24",
          "product": "salaried",
          "consent": true,
          "notes": "ok",
          "kyc_doc": "https://example.com/kyc/123"
        }
        """;
    var errors = jsonSchema.validate(readTree(payload));
    assertThat(errors).as("valid payload should produce no schema errors").isEmpty();
  }

  @Test
  void invalidPayloadFails() {
    JsonNode schema = generate(ALL_TYPES_YAML);
    JsonSchema jsonSchema = compile(schema);

    // loan_amount as string, product not in enum, consent as string, apply_date malformed.
    String payload =
        """
        {
          "applicant_name": "Priya",
          "loan_amount": "not-a-number",
          "apply_date": "yesterday",
          "product": "unknown",
          "consent": "yes"
        }
        """;
    var errors = jsonSchema.validate(readTree(payload));
    assertThat(errors).as("invalid payload should surface schema errors").isNotEmpty();
  }

  @Test
  void additionalPropertiesRejected() {
    JsonNode schema = generate(ALL_TYPES_YAML);
    JsonSchema jsonSchema = compile(schema);
    String payload =
        """
        {"applicant_name": "A", "loan_amount": 1, "rogue_key": "nope"}
        """;
    var errors = jsonSchema.validate(readTree(payload));
    assertThat(errors).isNotEmpty();
  }

  private JsonNode generate(String yaml) {
    var r = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    var result = validator.validate(r.raw(), r.lines());
    assertThat(result.isInvalid()).isFalse();
    return generator.generate(result.config().orElseThrow());
  }

  private JsonSchema compile(JsonNode schema) {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    return factory.getSchema(schema);
  }

  private JsonNode readTree(String json) {
    try {
      return mapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void requiredFieldsAppearInRequiredArray() {
    JsonNode schema = generate(ALL_TYPES_YAML);
    var required = schema.get("required");
    Set<String> required1 = new java.util.HashSet<>();
    required.elements().forEachRemaining(n -> required1.add(n.asText()));
    assertThat(required1).containsExactlyInAnyOrder("applicant_name", "loan_amount");
  }
}
