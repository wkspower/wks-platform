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
package com.wks.caseengine.repository;

/**
 * @author victor.franca
 *
 */
public class DatabaseRecordNotFoundException extends Exception {

	private static final String DEFAULT_MESSAGE = "Database record %s with %s %s not found";

	private static final long serialVersionUID = 1L;

	public DatabaseRecordNotFoundException(final String entityName, final String identifierName,
			final String identifierValue) {
		super(String.format(DEFAULT_MESSAGE, entityName, identifierName, identifierValue));
	}

}
