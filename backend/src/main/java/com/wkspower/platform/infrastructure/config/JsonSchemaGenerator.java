package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldOption;
import org.springframework.stereotype.Component;

/**
 * Builds a Draft 2020-12 JSON Schema describing the {@code data} payload for a case of a given
 * type. Plain Jackson {@link ObjectNode} tree — no third-party schema-builder library (the
 * seven-field-type surface does not justify the dep).
 *
 * <p>Key ordering is load-bearing: {@code $schema, type, properties, required,
 * additionalProperties}. Frontend snapshot tests in Stories 2.5/2.7 will parse the JSON text and
 * depend on insertion order (Jackson's {@code ObjectNode} preserves it).
 */
@Component
public class JsonSchemaGenerator {

  private static final String DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";
  private static final JsonNodeFactory NF = JsonNodeFactory.instance;

  public JsonNode generate(CaseTypeConfig config) {
    ObjectNode root = NF.objectNode();
    root.put("$schema", DRAFT_2020_12);
    root.put("type", "object");

    ObjectNode properties = root.putObject("properties");
    ArrayNode required = NF.arrayNode();

    for (FieldDefinition f : config.fields()) {
      properties.set(f.id(), fieldSchema(f));
      if (f.required()) {
        required.add(f.id());
      }
    }

    // Always emit the required array (may be empty) — stable shape across configs.
    root.set("required", required);
    root.put("additionalProperties", false);
    return root;
  }

  private ObjectNode fieldSchema(FieldDefinition f) {
    ObjectNode node = NF.objectNode();
    var slots = f.slots();
    switch (f.type()) {
      case TEXT, TEXTAREA -> {
        node.put("type", "string");
        if (slots != null && slots.maxLength() != null) {
          node.put("maxLength", slots.maxLength());
        }
        if (slots != null && slots.minLength() != null) {
          node.put("minLength", slots.minLength());
        }
      }
      case NUMBER -> {
        node.put("type", "number");
        if (slots != null && slots.min() != null) {
          node.put("minimum", slots.min());
        }
        if (slots != null && slots.max() != null) {
          node.put("maximum", slots.max());
        }
      }
      case DATE -> {
        node.put("type", "string");
        node.put("format", "date");
      }
      case SELECT -> {
        node.put("type", "string");
        ArrayNode enumNode = node.putArray("enum");
        for (FieldOption o : f.options()) {
          enumNode.add(o.value());
        }
      }
      case CHECKBOX -> node.put("type", "boolean");
      case FILE -> {
        node.put("type", "string");
        node.put("format", "uri");
      }
      default -> throw new IllegalStateException("Unsupported field type: " + f.type());
    }
    return node;
  }
}
