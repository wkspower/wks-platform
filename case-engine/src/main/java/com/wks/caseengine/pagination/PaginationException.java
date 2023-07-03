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
package com.wks.caseengine.pagination;

public class PaginationException extends RuntimeException {

	private static final long serialVersionUID = 5032038649247198464L;

	public PaginationException(String message) {
		super(message);
	}

	public PaginationException(String message, Exception ex) {
		super(message, ex);
	}

}
