package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.caseengine.bpm.BpmEngineService;
import com.wks.caseengine.rest.mocks.MockSecurityContext;
import com.wks.caseengine.rest.server.ProcessDefinitionController;

@WebMvcTest(controllers = ProcessDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProcessDeploymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BpmEngineClientFacade processEngineClient;

	@MockBean
	private BpmEngineService bpmEngineService;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}
	
	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(multipart("/process-deployment/create/{bpmEngineId}", "1").file("file", null))
				.andExpect(status().isOk());
	}

}
