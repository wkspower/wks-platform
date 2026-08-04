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

import java.util.List;
import java.util.Locale;

/**
 * Checks an uploaded diagram before it is stored.
 *
 * <h2>Why this rejects rather than cleans</h2>
 *
 * An SVG is a document that can carry script, event handlers and external
 * references, and it ends up inside a page. Writing an SVG sanitiser here would
 * mean reimplementing something subtle and easy to get quietly wrong — and a
 * half-right sanitiser is worse than none, because it invites everything
 * downstream to trust its output.
 *
 * <p>So this is a doorman, not a cleaner: a file carrying anything active is
 * refused outright, with a message saying what was found. Refusing is also the
 * honest response to an upload that should never contain such a thing — a diagram
 * exported by a modelling tool has no script in it, so one that does is either
 * hostile or not what the user thinks it is.
 *
 * <p>This is <em>not</em> the security boundary on its own. The renderer sanitises
 * again with a purpose-built library before injecting the markup, because the
 * value may have been stored before these rules existed, or written by a route
 * that did not come through here.
 *
 * @author victor.franca
 */
public final class SourceDiagramValidator {

	/**
	 * Generous next to a real diagram — the reference model exports at ~30 KB — and
	 * far below anything that would bloat a config document or a page.
	 */
	static final int MAX_LENGTH = 2 * 1024 * 1024;

	/**
	 * Markers for content that executes or reaches off-page. Matched
	 * case-insensitively against the raw text: an exported diagram legitimately
	 * contains none of them, so a plain substring test is enough to keep hostile
	 * uploads out of storage without pretending to be a parser.
	 */
	private static final List<String> FORBIDDEN = List.of("<script", "javascript:", "<foreignobject", "<iframe",
			"<embed", "<object", "<!entity");

	private SourceDiagramValidator() {
	}

	/**
	 * @throws IllegalArgumentException when the content is not a plain SVG diagram
	 */
	public static void validate(final String svg) {
		if (svg == null || svg.isBlank()) {
			throw new IllegalArgumentException("No diagram content was supplied.");
		}

		if (svg.length() > MAX_LENGTH) {
			throw new IllegalArgumentException(
					"The diagram is larger than " + (MAX_LENGTH / 1024 / 1024) + " MB and was not stored.");
		}

		String lower = svg.toLowerCase(Locale.ROOT);

		if (!lower.contains("<svg")) {
			throw new IllegalArgumentException(
					"That file is not an SVG diagram. Export the model's diagram as SVG and attach that.");
		}

		for (String marker : FORBIDDEN) {
			if (lower.contains(marker)) {
				throw new IllegalArgumentException("The diagram contains '" + marker.replace("<", "")
						+ "', which is not allowed in a stored diagram. Export it from the modelling tool "
						+ "without embedded scripts or external references.");
			}
		}

		// Inline event handlers are the other way markup executes. Checked separately
		// because the attribute name varies (onload, onclick, …) and only the "on"
		// prefix followed by an assignment is meaningful.
		if (lower.matches("(?s).*\\son[a-z]+\\s*=.*")) {
			throw new IllegalArgumentException(
					"The diagram contains an inline event handler, which is not allowed in a stored diagram.");
		}

		// A plain DOCTYPE is fine and every real export carries one — cmmn-js and the
		// other bpmn.io tools all emit `<!DOCTYPE svg PUBLIC …>`. What is dangerous is
		// an internal subset, which is where entity declarations live, so that is what
		// is refused. Rejecting the declaration outright would turn away every diagram
		// this feature exists to show.
		int doctype = lower.indexOf("<!doctype");
		if (doctype >= 0) {
			int close = lower.indexOf('>', doctype);
			String declaration = close > doctype ? lower.substring(doctype, close) : lower.substring(doctype);
			if (declaration.contains("[")) {
				throw new IllegalArgumentException(
						"The diagram declares internal entities, which is not allowed in a stored diagram.");
			}
		}
	}

}
