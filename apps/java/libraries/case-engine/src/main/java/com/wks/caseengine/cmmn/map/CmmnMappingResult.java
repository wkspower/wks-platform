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
package com.wks.caseengine.cmmn.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wks.caseengine.cases.definition.CaseDefinition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What the mapper produces: the case definition, the work each stage should carry,
 * and everything that could not be mapped.
 *
 * <p>The task lists are kept beside the definition rather than on it because the
 * platform's case definition has no notion of a task — work lives in BPMN
 * processes. The generator turns these into one process per stage; keeping them
 * separate is what lets the mapping be tested without generating any XML.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CmmnMappingResult {

	private CaseDefinition caseDefinition;

	/** Ordered task names per stage name — the work the stage's process must hold. */
	@Default
	private Map<String, List<String>> stageTasks = new java.util.LinkedHashMap<>();

	/** Discretionary task names per stage name — manually startable, not gating. */
	@Default
	private Map<String, List<String>> stageDiscretionaryTasks = new java.util.LinkedHashMap<>();

	@Default
	private List<CmmnImportWarning> warnings = new ArrayList<>();

	public boolean hasWarnings() {
		return warnings != null && !warnings.isEmpty();
	}

}
