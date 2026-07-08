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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.action.CaseAction;
import com.wks.caseengine.cases.definition.action.CaseStageUpdateAction;
import com.wks.caseengine.event.ActionHook;

/**
 * Beyond-schema structural checks — the internal consistency the JSON Schema can't
 * express. Pure and self-contained (no repository access): it only cross-references
 * fields within a single document. Repository-backed checks (e.g. does {@code formKey}
 * reference an existing Form) are intentionally out of scope here to avoid create-order
 * hazards; those belong at the command layer.
 */
@Component
public class ConfigStructuralValidator {

	/**
	 * @return structural violations for {@code document} (empty when it is internally
	 *         consistent). Only applies checks for types that have them today.
	 */
	public List<String> validate(ConfigDocType type, Object document) {
		if (type == ConfigDocType.CASE_DEFINITION && document instanceof CaseDefinition caseDefinition) {
			return validateCaseDefinition(caseDefinition);
		}
		return List.of();
	}

	private List<String> validateCaseDefinition(CaseDefinition caseDefinition) {
		List<String> violations = new ArrayList<>();

		Set<String> stageNames = new LinkedHashSet<>();
		if (caseDefinition.getStages() != null) {
			for (CaseStage stage : caseDefinition.getStages()) {
				if (stage != null && stage.getName() != null) {
					stageNames.add(stage.getName());
				}
			}
		}

		// A CASE_STAGE_UPDATE_ACTION moves a case to a stage BY NAME; that name must be
		// one of the definition's own stages, or the hook can never transition the case.
		if (caseDefinition.getCaseHooks() != null) {
			for (ActionHook hook : caseDefinition.getCaseHooks()) {
				if (hook == null || hook.getActions() == null) {
					continue;
				}
				for (CaseAction action : hook.getActions()) {
					if (action instanceof CaseStageUpdateAction stageAction) {
						String target = stageAction.getNewStage();
						if (target != null && !stageNames.contains(target)) {
							violations.add("caseHook action targets stage '" + target
									+ "' which is not one of the defined stages " + stageNames);
						}
					}
				}
			}
		}

		return violations;
	}
}
