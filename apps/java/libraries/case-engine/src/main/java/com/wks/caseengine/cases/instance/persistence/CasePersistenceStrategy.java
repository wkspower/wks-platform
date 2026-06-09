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
package com.wks.caseengine.cases.instance.persistence;

import com.wks.caseengine.cases.instance.CaseInstance;

/**
 * Strategy for how a newly created case instance is persisted, selected by
 * {@code wks.bpm.engine}. It replaces the procedural workflow-enabled branch in
 * {@code StartCaseInstanceWithValuesCmd}: with a workflow engine the case is
 * written via the BPM round-trip ({@code caseSave} external task); without one it
 * is written directly. The command autowires the single active strategy and
 * contains no branch.
 */
public interface CasePersistenceStrategy {

	/**
	 * Persists the prepared case instance according to the active strategy.
	 *
	 * @param preparedCaseInstance the fully built case instance (business key,
	 *                             attributes and initial stage already set)
	 * @return the persisted case instance
	 */
	CaseInstance persist(CaseInstance preparedCaseInstance);

}
