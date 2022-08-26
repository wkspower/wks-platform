package com.wks.caseengine.cases.definition.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.caseengine.process.instance.ProcessInstanceService;

@Component
public class CaseEventExecutor {

	@Autowired
	private ProcessInstanceService processInstanceService;

	public void execute(final CaseEvent caseEvent, final String businessKey) {
		execute(caseEvent, businessKey, null);
	}

	public void execute(final CaseEvent caseEvent, final String businessKey, final JsonObject eventPayload) {

		//TODO Refactor as strategy
		if (CaseEventType.PROCESS_START.equals(caseEvent.getType())) {
			processInstanceService.create(caseEvent.getEventPayload().get("processDefinitionKey").getAsString(),
					businessKey);
		} else {
			throw new RuntimeException("Case Event Type not found");
		}
	}

	public void setProcessInstanceService(ProcessInstanceService processInstanceService) {
		this.processInstanceService = processInstanceService;
	}
}
