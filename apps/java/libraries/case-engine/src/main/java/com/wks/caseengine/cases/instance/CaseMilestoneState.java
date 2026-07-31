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
package com.wks.caseengine.cases.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A milestone this case has reached.
 *
 * <p>Only achieved milestones are recorded — the absence of an entry means "not
 * yet reached", so there is no pending/unreached state to keep in sync with the
 * case definition. That also means a milestone removed from the definition leaves
 * historical achievements intact, which is the honest record of what happened.
 *
 * <p>The {@code name} is denormalized deliberately: it is what the milestone was
 * called when it was reached, so renaming it in the definition does not rewrite
 * history.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CaseMilestoneState {

	/** Id of the achieved {@code CaseMilestone}. */
	private String id;

	/** Milestone name as it stood when the milestone was reached. */
	private String name;

	/** When it was reached, ISO-8601 UTC. */
	private String achievedAt;

}
