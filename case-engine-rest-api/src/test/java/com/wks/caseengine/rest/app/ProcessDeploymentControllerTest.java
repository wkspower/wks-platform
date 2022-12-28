package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
public class ProcessDeploymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BpmEngineClientFacade processEngineClient;

	@MockBean
	private BpmEngineService bpmEngineService;

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(multipart("/process-deployment/create/{bpmEngineId}", "1").file("file", null))
				.andExpect(status().isOk());
	}

}