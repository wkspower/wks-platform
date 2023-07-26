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
package com.wks.caseengine.tasks.event.complete;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum CaseEventType implements Serializable {

	@SerializedName("TASK_COMPLETE_EVENT_TYPE")
	TASK_COMPLETE_EVENT_TYPE("TASK_COMPLETE_EVENT_TYPE");
	
	private final String code;

	CaseEventType(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
