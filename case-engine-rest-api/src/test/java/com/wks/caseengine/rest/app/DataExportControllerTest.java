package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.data.export.DataExportService;
import com.wks.caseengine.rest.server.VariableController;

@WebMvcTest(controllers = VariableController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataExportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DataExportService dataExportService;

	@Test
	public void testExport() throws Exception {
		this.mockMvc.perform(get("/export/")).andExpect(status().isOk());
	}
}
