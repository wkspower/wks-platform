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
package com.wks.caseengine.loader.schema;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Anti-drift gate: every config entry shipped in the demo seed must conform to
 * the canonical JSON Schemas published in
 * {@code packages/case-config-schema/schemas} (the WKS Case Configuration
 * Standard). This is what keeps the schema, the seed data, and the Java model
 * from silently diverging. Named {@code *Test} so Surefire runs it during
 * {@code mvn package} (this repo configures no Failsafe plugin).
 */
class ConfigSchemaValidationTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final JsonSchemaFactory FACTORY =
			JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

	@Test
	void seedConfigsConformToTheStandard() throws Exception {
		Path schemasDir = locateSchemasDir();
		Path seedFile = new File("data/mongodb/mongo-base-collections.json").getAbsoluteFile().toPath();
		assertTrue(Files.exists(seedFile), "Seed file not found at " + seedFile);

		JsonNode seed = MAPPER.readTree(seedFile.toFile());

		validateArray(seed, "caseDefinition", schemasDir.resolve("case-definition.schema.json"), "id");
		validateArray(seed, "form", schemasDir.resolve("form.schema.json"), "key");
		validateArray(seed, "queue", schemasDir.resolve("queue.schema.json"), "id");
	}

	private void validateArray(JsonNode seed, String collection, Path schemaFile, String idField)
			throws Exception {
		JsonNode array = seed.get(collection);
		if (array == null || !array.isArray()) {
			fail("Seed is missing the '" + collection + "' array");
		}
		JsonSchema schema = loadSchema(schemaFile);
		for (int i = 0; i < array.size(); i++) {
			JsonNode entry = array.get(i);
			Set<ValidationMessage> errors = schema.validate(entry);
			if (!errors.isEmpty()) {
				String id = entry.hasNonNull(idField) ? entry.get(idField).asText() : "#" + i;
				fail("Seed " + collection + " [" + id + "] violates the Standard: " + errors);
			}
		}
	}

	private JsonSchema loadSchema(Path schemaFile) throws Exception {
		assertTrue(Files.exists(schemaFile), "Schema not found: " + schemaFile);
		try (InputStream in = Files.newInputStream(schemaFile)) {
			return FACTORY.getSchema(in);
		}
	}

	/**
	 * Walk up from the module working directory until the published schemas
	 * directory is found. Works regardless of which reactor module CWD the test
	 * is launched from.
	 */
	private Path locateSchemasDir() {
		File dir = new File(".").getAbsoluteFile();
		while (dir != null) {
			File candidate = new File(dir, "packages/case-config-schema/schemas");
			if (candidate.isDirectory()) {
				return candidate.toPath();
			}
			dir = dir.getParentFile();
		}
		throw new IllegalStateException(
				"Could not locate packages/case-config-schema/schemas above " + new File(".").getAbsolutePath()
						+ " — the WKS Case Configuration Standard schemas must be present in the repo.");
	}

}
