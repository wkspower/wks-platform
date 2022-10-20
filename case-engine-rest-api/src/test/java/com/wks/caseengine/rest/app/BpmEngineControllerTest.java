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

import com.wks.caseengine.bpm.BpmEngineService;
import com.wks.caseengine.rest.server.BpmEngineController;

@WebMvcTest(controllers = BpmEngineController.class)
public class BpmEngineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BpmEngineService bpmEngineService;

	@Test
	public void testSave() throws Exception {
		this.mockMvc.perform(post("/bpm-engine/").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testDelete() throws Exception {
		this.mockMvc.perform(delete("/bpm-engine/{bpmEngineId}", "1")).andExpect(status().isOk());
	}

	@Test
	public void testUpdate() throws Exception {
		this.mockMvc
				.perform(patch("/bpm-engine/{bpmEngineId}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/bpm-engine/")).andExpect(status().isOk());
	}

}
