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
package com.wks.bpm.engine.client;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.BpmEngineType;

public class DefaultC7BpmEngine extends BpmEngine {

	private String DEFAULT_C7_BPM_ID = "default-camunda7-engine";
	private String DEFAULT_C7_BPM_NAME = "Default Camunda 7";

	@Override
	public String getId() {
		return DEFAULT_C7_BPM_ID;
	}

	@Override
	public String getName() {
		return DEFAULT_C7_BPM_NAME;
	}

	@Override
	public BpmEngineType getType() {
		return BpmEngineType.BPM_ENGINE_CAMUNDA7;
	}

}
