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
package com.wks.bpm.engine.exception;

/**
 * Raised when the BPM engine rejects a deployment.
 *
 * <p>Unchecked on purpose: it travels up through the {@code BpmEngineClient} /
 * {@code BpmEngineClientFacade} contracts without changing their signatures, so
 * the REST layer can turn an engine-side rejection into a real HTTP error
 * instead of the deployment failing silently behind a 204.
 */
public class BpmEngineDeploymentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BpmEngineDeploymentException(final String message) {
		super(message);
	}

	public BpmEngineDeploymentException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
