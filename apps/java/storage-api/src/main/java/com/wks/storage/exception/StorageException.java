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
package com.wks.storage.exception;

public class StorageException extends RuntimeException {

	private static final long serialVersionUID = -3644218680986166831L;

	public StorageException(Exception e) {
		super(e);
	}

}
