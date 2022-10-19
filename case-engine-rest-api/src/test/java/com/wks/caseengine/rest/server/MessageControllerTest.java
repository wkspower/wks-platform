package com.wks.caseengine.rest.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.process.message.MessageSenderService;

@WebMvcTest(controllers = MessageController.class)
public class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MessageSenderService messageSenderService;

	@Test
	public void testSave() throws Exception {
		this.mockMvc.perform(post("/message/{bpmEngineId}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

}
