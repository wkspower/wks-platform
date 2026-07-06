/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.rest.config.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Validates a case-configuration document against its canonical JSON Schema —
 * the same draft-2020-12 schemas that back the {@code @wkspower/case-config-schema}
 * package and the seed anti-drift gate, so backend enforcement and the published
 * Standard share one source of truth.
 *
 * <p>Schemas are compiled once at construction from the classpath copy under
 * {@code wks-config-schemas/}. Stateless and thread-safe thereafter.
 */
@Component
public class ConfigSchemaValidator {

	private final Map<ConfigDocType, JsonSchema> schemas = new EnumMap<>(ConfigDocType.class);

	public ConfigSchemaValidator() {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
		for (ConfigDocType type : ConfigDocType.values()) {
			schemas.put(type, load(factory, type));
		}
	}

	private JsonSchema load(JsonSchemaFactory factory, ConfigDocType type) {
		String resource = type.schemaClasspathResource();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				throw new IllegalStateException("Bundled config schema not found on classpath: " + resource
						+ " — the wks-config-schemas resources must be packaged with the service.");
			}
			return factory.getSchema(in);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read bundled config schema: " + resource, e);
		}
	}

	/**
	 * @return the schema violations for {@code document}, sorted for stable output;
	 *         an empty list means the document conforms.
	 */
	public List<String> validate(ConfigDocType type, JsonNode document) {
		Set<ValidationMessage> messages = schemas.get(type).validate(document);
		// TreeSet(String) → deterministic ordering, independent of the validator's
		// internal iteration order, so error messages and tests stay stable.
		Set<String> sorted = new TreeSet<>();
		for (ValidationMessage m : messages) {
			sorted.add(m.getMessage());
		}
		return new ArrayList<>(sorted);
	}
}
