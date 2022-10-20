package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.cases.instance.CaseInstanceService;
import com.wks.caseengine.rest.server.CaseController;

@WebMvcTest(controllers = CaseController.class)
public class CaseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CaseInstanceService caseInstanceService;

	@Test
	public void testSave() throws Exception {
		this.mockMvc.perform(post("/case/").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testDelete() throws Exception {
		this.mockMvc.perform(delete("/case/{caseDefId}", "1")).andExpect(status().isOk());
	}

	@Test
	public void testUpdate() throws Exception {
		this.mockMvc.perform(patch("/case/{caseDefId}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(get("/case/{caseDefId}", "1")).andExpect(status().isOk());
	}

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/case/")).andExpect(status().isOk());
	}

}
