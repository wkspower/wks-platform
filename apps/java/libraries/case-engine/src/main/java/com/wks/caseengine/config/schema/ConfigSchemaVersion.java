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
package com.wks.caseengine.config.schema;

/**
 * Single source of truth for the WKS configuration Standard version carried by
 * each config document via its {@code schemaVersion} field.
 */
public final class ConfigSchemaVersion {

	/**
	 * The current WKS configuration Standard version.
	 */
	public static final String CURRENT = "2.2";

	/**
	 * Version assumed for a document that carries no {@code schemaVersion}. Such a
	 * document predates versioning, so it is the {@code 1.0} baseline — NOT the
	 * current version.
	 */
	public static final String BASELINE = "1.0";

	private ConfigSchemaVersion() {
	}

	/**
	 * Tolerant read of a document's {@code schemaVersion}: returns {@link #BASELINE}
	 * when the supplied value is {@code null} or blank (an unstamped document is
	 * pre-versioning), otherwise returns it as-is.
	 *
	 * @param v the raw schemaVersion read from a document, possibly {@code null}
	 * @return the normalized schema version, never {@code null}
	 */
	public static String normalize(String v) {
		return (v == null || v.isBlank()) ? BASELINE : v;
	}

}
