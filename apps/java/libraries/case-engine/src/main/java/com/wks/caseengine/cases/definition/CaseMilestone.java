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
package com.wks.caseengine.cases.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A milestone declared on a {@link CaseStage}: a named point of progress that is
 * recorded on the case once reached.
 *
 * <p>Milestones are deliberately independent of stage transitions. A stage may
 * declare several, and achieving one does not move the case — that is what makes
 * them able to express "this much is done" at a finer grain than the stage list,
 * which is a single ordered position.
 *
 * <p>Part of the WKS Case Configuration Standard since schema 2.2.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CaseMilestone {

	/**
	 * Stable identifier (slug). Case instances record achievement by this id and
	 * automation signals it by this id, so it must not change once cases exist.
	 */
	private String id;

	/** Human-readable name, shown on the case. Displayed as authored. */
	private String name;

	/**
	 * Optional note about what achieving this milestone means — e.g. carried over
	 * from an annotation in an imported source model.
	 */
	private String note;

	/**
	 * Optional id of the element this milestone was generated from in an imported
	 * source model (e.g. a CMMN Milestone's planItem id). Lets a viewer highlight
	 * the original diagram; absent for hand-authored milestones.
	 */
	private String sourceElementId;

}
