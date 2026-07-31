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

/**
 * Something the importer could not carry over faithfully.
 *
 * <p>Warnings are a deliverable, not an error channel. CMMN is a richer language
 * than the platform's stage model, so any real import leaves something behind;
 * saying exactly what — and against which element the modeller drew — is what lets
 * someone trust the result and hand-model the remainder. An import that reported
 * nothing would be claiming a fidelity it does not have.
 *
 * @param code      stable machine-readable kind, for grouping in the UI
 * @param elementId id of the source element this concerns, or null when general
 * @param message   human-readable explanation, shown to whoever imported the file
 *
 * @author victor.franca
 */
public record CmmnImportWarning(CmmnImportWarning.Code code, String elementId, String message) {

	public enum Code {

		/** A criterion was drawn with no condition wired; the importer inferred one. */
		INFERRED_CRITERION,

		/** A top-level item was folded into a stage because stages here are linear. */
		FOLDED_INTO_STAGE,

		/** Two or more concurrent stages had to be merged into one ordered stage. */
		CONCURRENT_STAGES_MERGED,

		/** A stage name had to be synthesized because no CMMN stage covered the items. */
		SYNTHESIZED_STAGE,

		/** A discretionary item became a manually-startable process. */
		DISCRETIONARY_ITEM,

		/** A manual-activation rule was not honoured. */
		MANUAL_ACTIVATION_IGNORED,

		/** A sentry condition expression has no equivalent and was dropped. */
		CONDITION_DROPPED,

		/** An exit criterion was dropped — there is no early termination. */
		EXIT_CRITERION_DROPPED,

		/** A repetition rule was dropped. */
		REPETITION_DROPPED,

		/** A lifecycle event other than complete/occur was dropped. */
		UNSUPPORTED_EVENT_DROPPED,

		/** An element kind the platform cannot represent was dropped. */
		UNSUPPORTED_ELEMENT_DROPPED,

		/** A cross-stage dependency became an ordering assumption. */
		DEPENDENCY_FLATTENED

	}

	public static CmmnImportWarning of(final Code code, final String elementId, final String message) {
		return new CmmnImportWarning(code, elementId, message);
	}

}
