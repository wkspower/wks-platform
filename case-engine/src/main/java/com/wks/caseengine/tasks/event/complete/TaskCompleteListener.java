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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskCompleteListener {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@EventListener
	public void onApplicationEvent(TaskCompleteEvent event) {
		TaskCompleteEventObject taskCompleteEventObject = (TaskCompleteEventObject) event.getSource();

		String businessKey = taskCompleteEventObject.getBusinessKey();
		String processDefKey = taskCompleteEventObject.getProcessDefinitionKey();
		String tskDefKey = taskCompleteEventObject.getTaskDefKey();

		executeCaseDefinitionHooks(businessKey, processDefKey, tskDefKey);
	}

	private void executeCaseDefinitionHooks(final String businessKey, final String processDefKey,
			final String tskDefKey) {

		try {
			CaseInstance caseInstance = caseInstanceService.get(businessKey);

			final String caseDefId = caseInstance.getCaseDefinitionId();
			CaseDefinition caseDefinition = caseDefinitionService.get(caseDefId);

			List<TaskCompleteHook> taskCompleteHooks = caseDefinition.getCaseHooks().stream()
					.filter(o -> CaseEventType.TASK_COMPLETE_EVENT_TYPE.equals(o.getEventType()))
					.map(TaskCompleteHook.class::cast).toList();

			taskCompleteHooks.stream().filter(hook -> processDefKey.startsWith(hook.getProcessDefKey()))
					.filter(hook -> hook.getTaskDefKey().equals(tskDefKey)).forEach(hook -> {
						hook.getActions().forEach(action -> action.visit(caseInstance));
					});

			caseInstanceService.update(caseInstance);
		} catch (Exception e) {
			log.error("CaseEventListener.onApplicationEvent: Case Instance {} not found when completing task {}",
					businessKey, tskDefKey);

			// TODO error handling
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

}
