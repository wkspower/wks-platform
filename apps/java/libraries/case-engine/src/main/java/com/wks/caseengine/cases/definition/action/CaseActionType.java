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
package com.wks.caseengine.cases.definition.action;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum CaseActionType implements Serializable {

	@SerializedName("CASE_STAGE_UPDATE_ACTION")
	CASE_STAGE_UPDATE_ACTION("CASE_STAGE_UPDATE_ACTION"),

	@SerializedName("CASE_QUEUE_UPDATE_ACTION")
	CASE_QUEUE_UPDATE_ACTION("CASE_QUEUE_UPDATE_ACTION");

	private final String code;

	CaseActionType(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
