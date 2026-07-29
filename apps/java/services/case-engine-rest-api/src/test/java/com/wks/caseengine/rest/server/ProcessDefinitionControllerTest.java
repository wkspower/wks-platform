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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@WebMvcTest(controllers = ProcessDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProcessDefinitionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BpmEngineClientFacade processEngineClient;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testGet() throws Exception {
		this.mockMvc.perform(get("/process-definition/{processDefinitionId}/xml", "1")).andExpect(status().isOk());
	}

	/**
	 * Regression: this is the exact body the case form's manual "start process"
	 * action sends — a business key and no process variables. It has to reach the
	 * engine as an empty variable list, not blow up on the way.
	 */
	@Test
	public void shouldStartProcessWithoutProcessVariables() throws Exception {
		this.mockMvc.perform(post("/process-definition/key/{key}/start", "demoProc")
				.contentType(MediaType.APPLICATION_JSON).content("{\"businessKey\":\"CASE-1\"}"))
				.andExpect(status().isOk());

		verify(processEngineClient).startProcess(eq("demoProc"), eq(Optional.of("CASE-1")),
				eq(Optional.<ProcessVariable>empty()));
	}

}
