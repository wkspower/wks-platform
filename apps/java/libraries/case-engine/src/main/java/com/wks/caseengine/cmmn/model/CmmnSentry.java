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
 * A CMMN sentry: the condition under which a criterion fires.
 *
 * <p>Only the {@code onPart} references are modelled, because that is the part the
 * platform can express — "these items reached this state". An {@code ifPart}
 * expression has no equivalent and is recorded merely so the mapper can report it
 * as dropped, carrying the expression text so it can be re-modelled by hand.
 *
 * <p>A sentry with no parts at all is meaningful in practice: it is what a
 * modeller leaves behind when they draw a criterion without wiring it. The mapper
 * treats that as "infer it" rather than "no condition".
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CmmnSentry {

	private String id;

	/** Plan-item events this sentry waits on. */
	@Default
	private List<CmmnOnPart> onParts = new ArrayList<>();

	/** The {@code ifPart} condition expression, when present. Unmappable. */
	private String ifPartCondition;

	/** True when nothing was wired to this sentry — the mapper must infer it. */
	public boolean isUnwired() {
		return (onParts == null || onParts.isEmpty()) && ifPartCondition == null;
	}

	/**
	 * One {@code planItemOnPart}: the referenced plan item plus the lifecycle event
	 * awaited on it (e.g. {@code complete} for a task, {@code occur} for a
	 * milestone).
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Setter
	@Builder
	@ToString
	public static class CmmnOnPart {

		private String sourceRef;

		private String standardEvent;

		/**
		 * Whether this is an event the platform can act on. A generated process can
		 * wait for work to finish or a milestone to be reached; the rest of the CMMN
		 * lifecycle vocabulary (enable, disable, suspend, terminate, …) has no
		 * equivalent.
		 */
		public boolean isSupportedEvent() {
			return "complete".equals(standardEvent) || "occur".equals(standardEvent);
		}

	}

}
