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
package com.wks.bpm.engine;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum BpmEngineType implements Serializable {

	@SerializedName("BPM_ENGINE_CAMUNDA7")
	BPM_ENGINE_CAMUNDA7("BPM_ENGINE_CAMUNDA7", "Camunda 7", "url"),

	@SerializedName("BPM_ENGINE_CAMUNDA8")
	BPM_ENGINE_CAMUNDA8("BPM_ENGINE_CAMUNDA8", "Camunda 8", "cluster");

	private final String code;

	private final String description;

	private String parametersNames;

	BpmEngineType(final String code, final String description, final String parametersNames) {
		this.code = code;
		this.description = description;
		this.parametersNames = parametersNames;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public String getParametersNames() {
		return parametersNames;
	}
}
