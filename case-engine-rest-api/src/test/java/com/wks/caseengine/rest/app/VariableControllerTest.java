package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.wks.caseengine.rest.mocks.MockSecurityContext;
import com.wks.caseengine.rest.server.VariableController;
import com.wks.caseengine.variables.VariableService;

@WebMvcTest(controllers = VariableController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VariableControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VariableService variableService;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/variable/{processInstanceId}", "1")).andExpect(status().isOk());
	}

}
