package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.rest.server.TaskController;
import com.wks.caseengine.tasks.TaskService;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TaskService taskService;

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/task/").param("bpmEngineId", "1")).andExpect(status().isOk());
	}

	@Test
	public void testClaim() throws Exception {
		this.mockMvc.perform(post("/task/{bpmEngineId}/{taskId}/claim/{taskAssignee}", "1", "2", "3"))
				.andExpect(status().isOk());
	}

	@Test
	public void testUnclaim() throws Exception {
		this.mockMvc.perform(post("/task/{bpmEngineId}/{taskId}/unclaim", "1", "2")).andExpect(status().isOk());
	}

	@Test
	public void testComplete() throws Exception {
		this.mockMvc.perform(post("/task/{bpmEngineId}/{taskId}/complete", "1", "2")
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
	}

}
