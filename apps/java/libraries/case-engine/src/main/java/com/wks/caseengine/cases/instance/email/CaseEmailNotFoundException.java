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
package com.wks.caseengine.cases.instance.email;

/**
 * @author victor.franca
 *
 */
public class CaseEmailNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MESSAGE = "Case Email not found";

	public CaseEmailNotFoundException(final Throwable e) {
		super(DEFAULT_MESSAGE, e);
	}

	public CaseEmailNotFoundException(final String message, final Throwable e) {
		super(message, e);
	}

}
