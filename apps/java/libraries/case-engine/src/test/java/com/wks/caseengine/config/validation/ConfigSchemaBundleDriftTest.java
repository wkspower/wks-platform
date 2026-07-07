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
package com.wks.caseengine.config.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Anti-drift gate for the schema copy the service ships. The runtime validator
 * loads schemas from the classpath ({@code wks-config-schemas/}) because the
 * repo-root {@code packages/} dir is outside the service's Docker build context.
 * This test proves that bundled copy is identical to the single source of truth
 * in {@code packages/case-config-schema/schemas}, so the two can never silently
 * diverge. Runs in the reactor build (where {@code packages/} is present); the
 * Docker image build skips tests, so the absent {@code packages/} dir is a non-issue.
 */
class ConfigSchemaBundleDriftTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void bundledSchemasMatchThePublishedSource() throws Exception {
		Path source = locateSchemasDir();
		for (ConfigDocType type : ConfigDocType.values()) {
			JsonNode published = MAPPER.readTree(source.resolve(type.schemaFileName()).toFile());
			JsonNode bundled = readBundled(type);
			assertEquals(published, bundled,
					type.schemaFileName() + " bundled on the service classpath has drifted from "
							+ "packages/case-config-schema/schemas — re-copy it so backend enforcement and the "
							+ "published Standard stay in lock-step.");
		}
	}

	private JsonNode readBundled(ConfigDocType type) throws Exception {
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(type.schemaClasspathResource())) {
			assertNotNull(in, "Bundled schema missing from classpath: " + type.schemaClasspathResource());
			return MAPPER.readTree(in);
		}
	}

	private Path locateSchemasDir() {
		File dir = new File(".").getAbsoluteFile();
		while (dir != null) {
			File candidate = new File(dir, "packages/case-config-schema/schemas");
			if (candidate.isDirectory()) {
				return candidate.toPath();
			}
			dir = dir.getParentFile();
		}
		throw new IllegalStateException("Could not locate packages/case-config-schema/schemas above "
				+ new File(".").getAbsolutePath());
	}

	@Test
	void allSchemaFilesExistInBoth() {
		Path source = locateSchemasDir();
		for (ConfigDocType type : ConfigDocType.values()) {
			assertTrue(Files.exists(source.resolve(type.schemaFileName())),
					"Source schema missing: " + type.schemaFileName());
		}
	}
}
