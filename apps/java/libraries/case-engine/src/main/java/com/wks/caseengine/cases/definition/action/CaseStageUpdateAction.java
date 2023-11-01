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

import com.wks.caseengine.cases.instance.CaseInstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CaseStageUpdateAction implements CaseAction {
	
	private String id;

	@Default
	private CaseActionType actionType = CaseActionType.CASE_STAGE_UPDATE_ACTION;

	private String newStage;
	

	@Override
	public void visit(CaseInstance caseInstance) {
		caseInstance.setStage(newStage);
	}


}
