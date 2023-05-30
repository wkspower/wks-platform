package com.wks.caseengine.tasks.event.complete;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TaskCompleteEventObject {

	private String processDefinitionKey;
	private String taskDefKey;
	private String businessKey;

}
