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
package com.wks.caseengine.rest.config.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.json.GsonBuilderFactory;
import com.wks.caseengine.rest.exception.GlobalExceptionHandler;
import com.wks.caseengine.rest.server.CaseDefinitionController;

/**
 * Proves the headline deliverable end-to-end through the HTTP layer: a malformed
 * case definition is rejected with 400 (via {@link GlobalExceptionHandler}), while
 * a conforming one is accepted — with enforcement in its default {@code enforce}
 * mode. Standalone MockMvc keeps it deterministic (no Spring context / no reliance
 * on which beans a @WebMvcTest slice would load).
 */
class ConfigEnforcementMvcTest {

	private MockMvc mockMvc;
	private CaseDefinitionService caseDefinitionService;

	@BeforeEach
	void setup() {
		caseDefinitionService = mock(CaseDefinitionService.class);
		Gson gson = new GsonBuilderFactory().getGsonBuilder().create();
		ConfigValidationService validation = new ConfigValidationService(new ConfigSchemaValidator(),
				new GsonBuilderFactory().getGsonBuilder(), "enforce");

		CaseDefinitionController controller = new CaseDefinitionController();
		ReflectionTestUtils.setField(controller, "caseDefinitionService", caseDefinitionService);
		ReflectionTestUtils.setField(controller, "configValidationService", validation);

		// Use Gson for HTTP conversion, exactly as the service does in production
		// (its models carry Gson types like JsonObject that Jackson can't serialize).
		this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setMessageConverters(new GsonHttpMessageConverter(gson))
				.build();
	}

	@Test
	void postMalformedCaseDefinition_isRejectedWith400() throws Exception {
		// Missing the required formKey.
		mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":\"customer-support\",\"name\":\"Customer Support\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Bad Request"));
	}

	@Test
	void postConformingCaseDefinition_isAccepted() throws Exception {
		when(caseDefinitionService.create(any())).thenReturn(new CaseDefinition());
		mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":\"customer-support\",\"name\":\"Customer Support\",\"formKey\":\"cs-form\"}"))
				.andExpect(status().isOk());
	}
}
