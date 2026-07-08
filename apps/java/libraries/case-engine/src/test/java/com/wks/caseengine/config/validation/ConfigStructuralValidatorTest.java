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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.action.CaseStageUpdateAction;
import com.wks.caseengine.tasks.event.complete.TaskCompleteHook;

class ConfigStructuralValidatorTest {

	private final ConfigStructuralValidator validator = new ConfigStructuralValidator();

	private CaseDefinition caseDefWithHookTargeting(String targetStage) {
		return CaseDefinition.builder()
				.id("customer-support")
				.name("Customer Support")
				.formKey("cs-form")
				.stages(List.of(
						CaseStage.builder().id("0").index(0).name("Intake").build(),
						CaseStage.builder().id("1").index(1).name("Resolution").build()))
				.caseHooks(List.of(TaskCompleteHook.builder()
						.actions(List.of(CaseStageUpdateAction.builder().newStage(targetStage).build()))
						.build()))
				.build();
	}

	@Test
	void hookTargetingAnExistingStage_isValid() {
		List<String> violations = validator.validate(ConfigDocType.CASE_DEFINITION,
				caseDefWithHookTargeting("Resolution"));
		assertTrue(violations.isEmpty(), () -> "expected no violations but got " + violations);
	}

	@Test
	void hookTargetingAnUndefinedStage_isReported() {
		List<String> violations = validator.validate(ConfigDocType.CASE_DEFINITION,
				caseDefWithHookTargeting("Nonexistent"));
		assertEquals(1, violations.size(), () -> "expected one violation but got " + violations);
		assertTrue(violations.get(0).contains("Nonexistent"));
	}

	@Test
	void otherDocTypes_haveNoStructuralChecks() {
		assertTrue(validator.validate(ConfigDocType.FORM, new Object()).isEmpty());
		assertTrue(validator.validate(ConfigDocType.CASE_DEFINITION, null).isEmpty());
	}
}
