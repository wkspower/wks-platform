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
package com.wks.caseengine.cmmn;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Turns a human-authored name into a safe identifier.
 *
 * <p>Shared by the mapper (milestone and case ids) and the BPMN generator (process
 * keys and element ids), which is why it lives here rather than on either of them.
 *
 * <p>Accents are decomposed rather than stripped, so German names keep their
 * vowels: "Anhörung angesetzt" becomes {@code anhorung-angesetzt}, not
 * {@code anh-rung-angesetzt}. These ids end up in BPMN attributes and process
 * variables where they are read by people debugging a case, so legibility matters.
 *
 * <p>Note what this must NOT be used for: the stage name written into a
 * {@code caseStageUpdate} variable. That is matched by string equality against the
 * case definition, so it has to stay verbatim.
 *
 * @author victor.franca
 */
public final class Slug {

	private static final String FALLBACK = "unnamed";

	private Slug() {
	}

	public static String of(final String value) {
		if (value == null || value.isBlank()) {
			return FALLBACK;
		}

		String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
				.replace("ß", "ss").toLowerCase(Locale.ROOT);

		String slug = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+)|(-+$)", "");

		return slug.isBlank() ? FALLBACK : slug;
	}

}
