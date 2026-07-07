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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionServiceImpl;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.json.GsonBuilderFactory;

/**
 * Proves enforcement is wired at the domain choke point: {@link CaseDefinitionServiceImpl}
 * validates on create/update before dispatching the command, so every caller of the
 * service (not just the REST controller) is covered. A non-conforming document is
 * rejected before the write command is ever executed.
 */
class CaseDefinitionServiceEnforcementTest {

	private CommandExecutor commandExecutor;
	private CaseDefinitionServiceImpl service;

	@BeforeEach
	void setup() {
		commandExecutor = mock(CommandExecutor.class);
		ConfigValidationService validation = new ConfigValidationService(new ConfigSchemaValidator(),
				new GsonBuilderFactory().getGsonBuilder(), "enforce");

		service = new CaseDefinitionServiceImpl();
		ReflectionTestUtils.setField(service, "commandExecutor", commandExecutor);
		ReflectionTestUtils.setField(service, "configValidationService", validation);
	}

	@Test
	void create_rejectsNonConforming_beforeExecutingTheCommand() {
		// Has an id (passes the id guard) but is missing the required formKey.
		CaseDefinition bad = CaseDefinition.builder().id("customer-support").name("Customer Support").build();

		assertThrows(ConfigValidationException.class, () -> service.create(bad));
		verify(commandExecutor, never()).execute(any());
	}

	@Test
	void create_allowsConforming() {
		CaseDefinition ok = CaseDefinition.builder().id("customer-support").name("Customer Support")
				.formKey("cs-form").build();

		assertDoesNotThrow(() -> service.create(ok));
		verify(commandExecutor).execute(any());
	}

	@Test
	void update_rejectsNonConforming() {
		CaseDefinition bad = CaseDefinition.builder().id("customer-support").name("Customer Support").build();
		assertThrows(ConfigValidationException.class, () -> service.update("customer-support", bad));
		verify(commandExecutor, never()).execute(any());
	}
}
