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

import com.wks.caseengine.cmmn.model.CmmnElement;

import lombok.Getter;

/**
 * One position in the linearized case: the case-plan-level elements that occupy
 * the same horizontal band of the diagram, and therefore become a single stage.
 *
 * <p>This exists because CMMN and the platform disagree about what a stage is. In
 * CMMN, stages are event-driven and several can be active at once; here, a case
 * sits in exactly one stage at a time and stages form an ordered list. The only
 * ordering signal a CMMN model carries is where the modeller placed things, so
 * left-to-right position is what we linearize on.
 *
 * @author victor.franca
 */
@Getter
public class CmmnStageBand {

	/** Case-plan-level elements in this band, in the order encountered. */
	private final List<CmmnElement> members = new ArrayList<>();

	private double left;

	private double right;

	CmmnStageBand(final CmmnElement first) {
		members.add(first);
		this.left = first.getBounds().x();
		this.right = first.getBounds().right();
	}

	/**
	 * Whether {@code element} overlaps this band horizontally. Overlap — rather than
	 * mere proximity — is the test, because a modeller who draws two things in the
	 * same column means them to be concurrent.
	 */
	boolean accepts(final CmmnElement element) {
		return element.getBounds().x() < right && left < element.getBounds().right();
	}

	void add(final CmmnElement element) {
		members.add(element);
		this.left = Math.min(left, element.getBounds().x());
		this.right = Math.max(right, element.getBounds().right());
	}

	/** The CMMN stages in this band. More than one means concurrency was merged. */
	public List<CmmnElement> stages() {
		return members.stream().filter(CmmnElement::isStage).toList();
	}

	public List<CmmnElement> looseTasks() {
		return members.stream().filter(CmmnElement::isHumanTask).toList();
	}

	public List<CmmnElement> looseMilestones() {
		return members.stream().filter(CmmnElement::isMilestone).toList();
	}

}
