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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.exception.BpmEngineDeploymentException;
import com.wks.caseengine.rest.exception.GlobalExceptionHandler;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

// Loads DeploymentController itself — this used to name ProcessDefinitionController,
// so the /deployment assertions never touched the controller under test.
@WebMvcTest(controllers = DeploymentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class DeploymentControllerTest {

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
	public void shouldDeploy() throws Exception {
		this.mockMvc.perform(post("/deployment").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	/**
	 * Regression: a model the engine refuses (e.g. one with no history TTL) used
	 * to come back as 204, so the portal closed the modeler and the user believed
	 * the process had been created. The rejection must reach the client.
	 */
	@Test
	public void shouldReportEngineRejectionInsteadOfSucceedingSilently() throws Exception {
		doThrow(new BpmEngineDeploymentException("ENGINE-12018 History Time To Live (TTL) cannot be null"))
				.when(processEngineClient).deploy(anyString(), anyString());

		this.mockMvc.perform(post("/deployment").contentType(MediaType.APPLICATION_JSON).content("<bpmn/>"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("ENGINE-12018")));
	}

}
