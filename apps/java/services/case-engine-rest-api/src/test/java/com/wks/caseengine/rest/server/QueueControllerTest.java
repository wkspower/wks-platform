/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.rest.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.wks.caseengine.queue.QueueService;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@WebMvcTest(controllers = QueueController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QueueControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QueueService queueService;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldSave() throws Exception {
		this.mockMvc.perform(post("/queue").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldDelete() throws Exception {
		this.mockMvc.perform(delete("/queue/{queueId}", "1")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldUpdate() throws Exception {
		this.mockMvc.perform(put("/queue/{queueIdKey}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldGet() throws Exception {
		this.mockMvc.perform(get("/queue/{queueId}", "1")).andExpect(status().isOk());
	}

	@Test
	public void shouldFind() throws Exception {
		this.mockMvc.perform(get("/queue")).andExpect(status().isOk());
	}

}
