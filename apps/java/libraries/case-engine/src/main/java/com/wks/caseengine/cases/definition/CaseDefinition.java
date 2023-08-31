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
package com.wks.caseengine.cases.definition;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.wks.caseengine.event.ActionHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CaseDefinition {

	public String id;

	private String name;

	private String formKey;

	private String stagesLifecycleProcessKey;

	private Boolean deployed;

	private List<CaseStage> stages;

	@Default
	private List<ActionHook> caseHooks = new ArrayList<>();

	@Default
	private JsonObject kanbanConfig = new JsonObject();

}
