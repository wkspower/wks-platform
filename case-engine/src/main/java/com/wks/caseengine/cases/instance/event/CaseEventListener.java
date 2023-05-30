package com.wks.caseengine.cases.instance.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionService;
import com.wks.caseengine.cases.definition.hook.TaskCompleteHook;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceService;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEvent;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEventObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CaseEventListener implements ApplicationListener<TaskCompleteEvent> {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@Override
	public void onApplicationEvent(TaskCompleteEvent event) {

		TaskCompleteEventObject taskCompleteEventObject = (TaskCompleteEventObject) event.getSource();

		String businessKey = taskCompleteEventObject.getBusinessKey();
		String processDefKey = taskCompleteEventObject.getProcessDefinitionKey();
		String tskDefKey = taskCompleteEventObject.getTaskDefKey();

		try {
			CaseInstance caseInstance = caseInstanceService.get(businessKey);

			final String caseDefId = caseInstance.getCaseDefinitionId();
			CaseDefinition caseDefinition = caseDefinitionService.get(caseDefId);
			List<TaskCompleteHook> taskCompleteHooks = caseDefinition.getTaskCompleteHooks();
			taskCompleteHooks.stream().filter(hook -> processDefKey.startsWith(hook.getProcessDefKey()))
					.filter(hook -> hook.getTaskDefKey().equals(tskDefKey)).forEach(hook -> {
						hook.getActions().forEach(action -> action.apply(caseInstance));
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
