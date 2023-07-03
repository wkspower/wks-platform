package com.wks.caseengine.rest.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.record.type.RecordTypeService;
import com.wks.caseengine.rest.mocks.MockSecurityContext;
import com.wks.caseengine.rest.server.RecordTypeController;

@WebMvcTest(controllers = RecordTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RecordTypeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RecordTypeService recordTypeService;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testSave() throws Exception {
		this.mockMvc.perform(post("/record-type/").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testDelete() throws Exception {
		this.mockMvc.perform(delete("/record-type/{id}", "1")).andExpect(status().isOk());
	}

	@Test
	public void testUpdate() throws Exception {
		this.mockMvc.perform(patch("/record-type/{id}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(get("/record-type/{id}", "1")).andExpect(status().isOk());
	}

	@Test
	public void testFind() throws Exception {
		this.mockMvc.perform(get("/record-type/")).andExpect(status().isOk());
	}

}
