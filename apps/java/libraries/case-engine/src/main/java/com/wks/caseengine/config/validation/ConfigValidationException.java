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
package com.wks.caseengine.config.validation;

import java.util.List;

import lombok.Getter;

/**
 * Thrown when a submitted configuration document violates its schema and the
 * validation mode is {@code enforce}. Translated to an HTTP 400 by
 * {@link com.wks.caseengine.rest.exception.GlobalExceptionHandler}.
 */
@Getter
public class ConfigValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final transient ConfigDocType docType;
	private final String documentId;
	private final transient List<String> violations;

	public ConfigValidationException(ConfigDocType docType, String documentId, List<String> violations) {
		super(buildMessage(docType, documentId, violations));
		this.docType = docType;
		this.documentId = documentId;
		this.violations = violations;
	}

	private static String buildMessage(ConfigDocType docType, String documentId, List<String> violations) {
		String who = docType.displayName() + (documentId != null ? " '" + documentId + "'" : "");
		return who + " does not conform to the WKS Case Configuration Standard: "
				+ String.join("; ", violations);
	}
}
