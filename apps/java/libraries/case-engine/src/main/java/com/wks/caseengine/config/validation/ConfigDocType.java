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

/**
 * The four case-configuration document types the WKS Case Configuration Standard
 * covers, each bound to its canonical JSON Schema. The schema files are bundled
 * on the classpath under {@code wks-config-schemas/} — a build-time copy of the
 * single source of truth in {@code packages/case-config-schema/schemas}, kept in
 * lock-step by {@code ConfigSchemaBundleDriftTest}.
 */
public enum ConfigDocType {

	CASE_DEFINITION("case-definition.schema.json", "Case Definition"),
	FORM("form.schema.json", "Form"),
	QUEUE("queue.schema.json", "Queue"),
	RECORD_TYPE("record-type.schema.json", "Record Type");

	/** Classpath location the schemas are bundled at (see the service Dockerfile / pom). */
	public static final String SCHEMA_CLASSPATH_DIR = "wks-config-schemas";

	private final String schemaFileName;
	private final String displayName;

	ConfigDocType(String schemaFileName, String displayName) {
		this.schemaFileName = schemaFileName;
		this.displayName = displayName;
	}

	public String schemaFileName() {
		return schemaFileName;
	}

	public String schemaClasspathResource() {
		return SCHEMA_CLASSPATH_DIR + "/" + schemaFileName;
	}

	public String displayName() {
		return displayName;
	}
}
