package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.rest.server.VariableController;
import com.wks.caseengine.rest.server.data.DataImportController;

@WebMvcTest(controllers = VariableController.class)

public class DataImportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DataImportController dataImportController;

	@Test
	public void testImport() throws Exception {
		this.mockMvc.perform(post("/import/").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());

	}
}
