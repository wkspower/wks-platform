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
package com.wks.bpm.externaltask.worker.impl;

/**
 * @author victor.franca
 *
 */
public enum BpmnErrorCode {

	CASE_NOT_FOUND("case-not-found"), CASE_DEFINITION_NOT_FOUND("case-def-not-found"),;

	private final String code;

	BpmnErrorCode(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
