package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.caseengine.bpm.BpmEngineService;
import com.wks.caseengine.rest.server.ProcessDefinitionController;

@WebMvcTest(controllers = ProcessDefinitionController.class)
public class ProcessDefinitionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BpmEngineClientFacade processEngineClient;

	@MockBean
	private BpmEngineService bpmEngineService;

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(get("/process-definition/{bpmEngineId}/{processDefinitionId}/xml", "1", "2"))
				.andExpect(status().isOk());
	}

}
