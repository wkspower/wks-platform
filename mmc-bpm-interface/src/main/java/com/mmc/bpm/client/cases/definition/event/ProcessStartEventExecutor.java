package com.mmc.bpm.client.cases.definition.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.client.process.instance.ProcessInstanceService;

@Component
public class ProcessStartEventExecutor {

	@Autowired
	private ProcessInstanceService processInstanceService;

	public void execute(final ProcessStartEvent processStartEvent, final String businessKey) {
		processInstanceService.create(processStartEvent.getProcessDefinitionKey(), businessKey);
	}

}
