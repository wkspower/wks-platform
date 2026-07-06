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

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Applies the WKS Case Configuration Standard at the API boundary: on create and
 * update, a config document is serialized to its canonical JSON form (via the same
 * Gson the persistence layer uses) and checked against its schema.
 *
 * <p>Rollout is governed by {@code wks.config.validation.mode}:
 * <ul>
 *   <li>{@code enforce} (default) — reject non-conforming documents with HTTP 400.</li>
 *   <li>{@code warn} — log violations but allow the write (transition mode).</li>
 *   <li>{@code disabled} — skip validation entirely.</li>
 * </ul>
 */
@Service
@Slf4j
public class ConfigValidationService {

	/** Jackson is present transitively (networknt); used only here to bridge Gson → JsonNode. */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public enum Mode {
		ENFORCE, WARN, DISABLED
	}

	private final ConfigSchemaValidator validator;
	private final Gson gson;
	private final Mode mode;

	public ConfigValidationService(ConfigSchemaValidator validator, GsonBuilder gsonBuilder,
			@Value("${wks.config.validation.mode:enforce}") String mode) {
		this.validator = validator;
		this.gson = gsonBuilder.create();
		this.mode = parseMode(mode);
		log.info("Config Standard validation mode: {}", this.mode);
	}

	private static Mode parseMode(String raw) {
		try {
			return Mode.valueOf(raw.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			log.warn("Unknown wks.config.validation.mode '{}'; defaulting to ENFORCE", raw);
			return Mode.ENFORCE;
		}
	}

	/**
	 * Validate a config document before it is persisted.
	 *
	 * @param type       the document's Standard type
	 * @param document   the deserialized config POJO (CaseDefinition/Form/Queue/RecordType)
	 * @param documentId best-effort identifier for diagnostics (may be {@code null})
	 * @throws ConfigValidationException when the document violates its schema and the
	 *                                   mode is {@code enforce}
	 */
	public void validateOnWrite(ConfigDocType type, Object document, String documentId) {
		if (mode == Mode.DISABLED || document == null) {
			return;
		}

		List<String> violations = validator.validate(type, toJsonNode(type, documentId, document));
		if (violations.isEmpty()) {
			return;
		}

		if (mode == Mode.WARN) {
			log.warn("Config Standard violation (warn-only) — {} '{}': {}", type.displayName(), documentId,
					String.join("; ", violations));
			return;
		}
		throw new ConfigValidationException(type, documentId, violations);
	}

	private JsonNode toJsonNode(ConfigDocType type, String documentId, Object document) {
		try {
			// Serialize with Gson (the canonical, persisted form) then re-parse with
			// Jackson, which is what networknt validates against.
			return MAPPER.readTree(gson.toJson(document));
		} catch (Exception e) {
			// A document that can't even be serialized to JSON can't be validated or
			// stored; surface it as a validation failure rather than a 500.
			throw new ConfigValidationException(type, documentId,
					List.of("could not be serialized to JSON: " + e.getMessage()));
		}
	}
}
