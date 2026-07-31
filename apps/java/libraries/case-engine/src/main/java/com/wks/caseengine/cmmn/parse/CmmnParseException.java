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
package com.wks.caseengine.cmmn.parse;

/**
 * Raised when a supplied document is not a CMMN model this importer can read.
 *
 * <p>Distinct from a mapping warning: a warning means "imported, with something
 * left behind", whereas this means "there is nothing to import". The message is
 * shown to whoever uploaded the file, so it says what is wrong with the document
 * rather than where the parser gave up.
 *
 * @author victor.franca
 */
public class CmmnParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CmmnParseException(final String message) {
		super(message);
	}

	public CmmnParseException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
