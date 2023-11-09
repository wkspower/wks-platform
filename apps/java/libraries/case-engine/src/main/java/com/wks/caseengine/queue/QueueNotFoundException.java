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
package com.wks.caseengine.queue;

/**
 * @author victor.franca
 *
 */
public class QueueNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MESSAGE = "Queue not found";

	public QueueNotFoundException() {
		super(DEFAULT_MESSAGE);
	}

	public QueueNotFoundException(final String message, Throwable e) {
		super(message, e);
	}

}
