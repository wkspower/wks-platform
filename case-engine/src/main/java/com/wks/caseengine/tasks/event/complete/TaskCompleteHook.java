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

import java.util.List;

import com.wks.caseengine.cases.definition.action.CaseAction;
import com.wks.caseengine.event.ActionHook;

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
public class TaskCompleteHook implements ActionHook {

	@Default
	private CaseEventType eventType = CaseEventType.TASK_COMPLETE_EVENT_TYPE;

	private String processDefKey;
	private String taskDefKey;

	private List<CaseAction> actions;

}
