package com.wks.caseengine.cases.definition.event;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.camunda.client.MockProcessEngineClient;
import com.wks.bpm.engine.camunda.client.ProcessEngineClient;
import com.wks.caseengine.process.instance.ProcessInstanceServiceImpl;

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
		assertEquals(1, processEngineClient.findProcessInstances(Optional.of("1")).length);
		assertEquals("1", processEngineClient.findProcessInstances(Optional.of("1"))[0].getBusinessKey());

	}

}
