package com.wks.caseengine.rest.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.variables.VariableService;

@WebMvcTest(controllers = VariableController.class)
public class VariableControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VariableService variableService;

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/variable/{bpmEngineId}/{processInstanceId}", "1", "2")).andExpect(status().isOk());
	}

}
