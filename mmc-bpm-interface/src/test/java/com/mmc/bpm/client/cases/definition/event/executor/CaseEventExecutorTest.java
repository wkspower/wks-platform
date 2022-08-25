package com.mmc.bpm.client.cases.definition.event.executor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mmc.bpm.client.cases.definition.event.CaseEvent;
import com.mmc.bpm.client.cases.definition.event.CaseEventExecutor;
import com.mmc.bpm.client.cases.definition.event.CaseEventType;
import com.mmc.bpm.client.process.instance.ProcessInstanceServiceImpl;
import com.mmc.bpm.engine.camunda.client.MockProcessEngineClient;
import com.mmc.bpm.engine.camunda.client.ProcessEngineClient;

public class CaseEventExecutorTest {

	@Test
	public void shouldExecuteEvent() {

		// Given
		String eventPayloadJson = "{ \"processDefinitionKey\": \"generic-process\" }";
		JsonObject eventPayloadJsonObject = new Gson().fromJson(eventPayloadJson, JsonObject.class);

		CaseEvent caseEvent = new CaseEvent(null, null, CaseEventType.PROCESS_START, eventPayloadJsonObject);

		CaseEventExecutor executor = new CaseEventExecutor();

		ProcessInstanceServiceImpl processInstanceService = new ProcessInstanceServiceImpl();
		ProcessEngineClient processEngineClient = new MockProcessEngineClient();
		processInstanceService.setProcessEngineClient(processEngineClient);
		executor.setProcessInstanceService(processInstanceService);

		// When
		executor.execute(caseEvent, "1");

		// Then
		assertEquals(1, processEngineClient.findProcessInstances("1").length);
		assertEquals("1", processEngineClient.findProcessInstances("1")[0].getBusinessKey());

	}

}
