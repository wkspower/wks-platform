package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.cases.definition.CaseDefinitionService;
import com.wks.caseengine.rest.server.HealthCheckController;

@WebMvcTest(controllers = HealthCheckController.class)
public class HealthCheckControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	CaseDefinitionService caseDefinitionService;

	@Test
	public void testCheck() throws Exception {
		this.mockMvc.perform(get("/healthCheck/")).andExpect(status().isOk());
	}

}
