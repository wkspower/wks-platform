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
package com.wks.caseengine.cmmn.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One plan item from a CMMN model, flattened into what the importer needs.
 *
 * <p>The {@code id} is the plan item's own id — not its definition's — because
 * that is what the diagram references, so it is the handle a viewer needs to
 * highlight the shape the customer drew.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CmmnElement {

	/** The plan item id (diagram-referenced), e.g. {@code PlanItem_181t6jt}. */
	private String id;

	private CmmnElementType type;

	/**
	 * Id of the definition element this plan item references. CMMN splits identity
	 * from kind: the name and type live on the definition, the diagram identity on
	 * the plan item, so both are kept.
	 */
	private String definitionRef;

	/** Display name, taken from the referenced definition. */
	private String name;

	/**
	 * The CMMN element name we could not map, when {@link #type} is
	 * {@code UNSUPPORTED} — carried so the warning can say what was dropped.
	 */
	private String unsupportedKind;

	/** Id of the enclosing stage, or null when it sits on the case plan model. */
	private String parentId;

	/** Position/size from diagram interchange; null when the model has no DI. */
	private CmmnBounds bounds;

	/**
	 * True when this item is discretionary — offered from a planning table for a
	 * human to start, rather than run as part of the stage's normal flow.
	 */
	private boolean discretionary;

	/** True when the item's control declares a manual-activation rule. */
	private boolean manualActivation;

	/** True when the item's control declares a repetition rule. */
	private boolean repetition;

	/** Criteria guarding entry to this item. */
	@Default
	private List<CmmnCriterion> entryCriteria = new ArrayList<>();

	/**
	 * Criteria guarding exit from this item. The platform has no early-termination
	 * concept, so these exist only to be reported as dropped.
	 */
	@Default
	private List<CmmnCriterion> exitCriteria = new ArrayList<>();

	public boolean isStage() {
		return CmmnElementType.STAGE.equals(type);
	}

	public boolean isMilestone() {
		return CmmnElementType.MILESTONE.equals(type);
	}

	public boolean isHumanTask() {
		return CmmnElementType.HUMAN_TASK.equals(type);
	}

}
