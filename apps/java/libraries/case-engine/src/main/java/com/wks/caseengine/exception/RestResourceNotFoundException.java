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
package com.wks.caseengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author victor.franca
 *
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RestResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RestResourceNotFoundException(final String message) {
		super(message);
	}

}
