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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.json.GsonBuilderFactory;
import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.record.type.RecordType;

/**
 * Exercises WKS Case Configuration Standard enforcement end-to-end: real schemas
 * (loaded from the bundled classpath copy), real Gson serialization, and the
 * enforce / warn / disabled rollout modes.
 */
class ConfigValidationServiceTest {

	private ConfigValidationService service(String mode) {
		return new ConfigValidationService(new ConfigSchemaValidator(),
				new GsonBuilderFactory().getGsonBuilder(), mode);
	}

	private CaseDefinition validCaseDefinition() {
		return CaseDefinition.builder().id("customer-support").name("Customer Support").formKey("cs-form").build();
	}

	private CaseDefinition invalidCaseDefinition() {
		// Missing the required formKey.
		return CaseDefinition.builder().id("customer-support").name("Customer Support").build();
	}

	@Test
	void enforce_allowsConformingCaseDefinition() {
		ConfigValidationService service = service("enforce");
		assertDoesNotThrow(() -> service.validateOnWrite(ConfigDocType.CASE_DEFINITION, validCaseDefinition(),
				"customer-support"));
	}

	@Test
	void enforce_rejectsNonConformingCaseDefinition() {
		ConfigValidationService service = service("enforce");
		ConfigValidationException ex = assertThrows(ConfigValidationException.class,
				() -> service.validateOnWrite(ConfigDocType.CASE_DEFINITION, invalidCaseDefinition(),
						"customer-support"));
		assertEquals(ConfigDocType.CASE_DEFINITION, ex.getDocType());
		assertEquals("customer-support", ex.getDocumentId());
		assertFalse(ex.getViolations().isEmpty(), "expected at least one schema violation");
	}

	@Test
	void warn_allowsNonConformingCaseDefinition() {
		ConfigValidationService service = service("warn");
		assertDoesNotThrow(() -> service.validateOnWrite(ConfigDocType.CASE_DEFINITION, invalidCaseDefinition(),
				"customer-support"));
	}

	@Test
	void disabled_skipsValidationEntirely() {
		ConfigValidationService service = service("disabled");
		assertDoesNotThrow(() -> service.validateOnWrite(ConfigDocType.CASE_DEFINITION, invalidCaseDefinition(),
				"customer-support"));
	}

	@Test
	void unknownMode_defaultsToEnforce() {
		ConfigValidationService service = service("bogus");
		assertThrows(ConfigValidationException.class,
				() -> service.validateOnWrite(ConfigDocType.CASE_DEFINITION, invalidCaseDefinition(), "x"));
	}

	@Test
	void enforce_validatesQueue() {
		ConfigValidationService service = service("enforce");
		assertDoesNotThrow(() -> service.validateOnWrite(ConfigDocType.QUEUE,
				Queue.builder().id("q1").name("Queue 1").build(), "q1"));
		assertThrows(ConfigValidationException.class, () -> service.validateOnWrite(ConfigDocType.QUEUE,
				Queue.builder().name("no id").build(), null));
	}

	@Test
	void enforce_validatesRecordType() {
		ConfigValidationService service = service("enforce");
		assertDoesNotThrow(() -> service.validateOnWrite(ConfigDocType.RECORD_TYPE,
				RecordType.builder().id("rt1").build(), "rt1"));
		assertThrows(ConfigValidationException.class, () -> service.validateOnWrite(ConfigDocType.RECORD_TYPE,
				RecordType.builder().build(), null));
	}
}
