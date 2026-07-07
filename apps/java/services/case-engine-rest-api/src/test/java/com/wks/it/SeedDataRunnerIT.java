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
package com.wks.it;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.rest.CaseEngineRestAPIApp;

/**
 * WP-1.1 acceptance: the minimal core boots with the demo case definitions seeded.
 *
 * <p>Boots the real application under the {@code minimal} profile group
 * ({@code db-h2,bpm-none,auth-dev,authz-off,single-tenant,seed}) — the zero-container
 * mode — so the actual {@code SeedDataRunner} runs through the real
 * {@code DataImportService -> CommandExecutor -> CommandContext} graph and persists
 * the bundled demo collections onto embedded H2. A MOCK web environment (no real
 * servlet container, but a web context so the dev-auth security chain wires) keeps
 * it light.
 */
@SpringBootTest(classes = CaseEngineRestAPIApp.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
		"spring.profiles.active=minimal" })
public class SeedDataRunnerIT {

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Test
	public void shouldSeedDemoCaseDefinitionsIntoH2() {
		List<CaseDefinition> defs = caseDefinitionRepository.find();
		assertFalse(defs.isEmpty(), "seeder must have inserted demo case definitions");
		assertTrue(defs.stream().anyMatch(d -> "customer-support".equals(d.getId())),
				"expected the 'customer-support' demo case definition to be seeded");
	}
}
