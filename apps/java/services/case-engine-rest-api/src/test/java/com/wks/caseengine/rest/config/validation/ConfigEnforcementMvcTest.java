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

import java.util.List;

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
import com.wks.caseengine.config.validation.ConfigDocType;
import com.wks.caseengine.config.validation.ConfigValidationException;
import com.wks.caseengine.json.GsonBuilderFactory;
import com.wks.caseengine.rest.exception.GlobalExceptionHandler;
import com.wks.caseengine.rest.server.CaseDefinitionController;

/**
 * Verifies the REST layer's responsibility now that enforcement lives in the
 * domain service: when the service rejects a write with a
 * {@link ConfigValidationException}, the controller + {@link GlobalExceptionHandler}
 * surface it as HTTP 400; a successful write returns 200. Standalone MockMvc keeps
 * it deterministic. The validation logic itself is covered in the case-engine
 * library (ConfigValidationServiceTest); its wiring into the service is covered by
 * CaseDefinitionServiceEnforcementTest.
 */
class ConfigEnforcementMvcTest {

	private MockMvc mockMvc;
	private CaseDefinitionService caseDefinitionService;

	@BeforeEach
	void setup() {
		caseDefinitionService = mock(CaseDefinitionService.class);
		Gson gson = new GsonBuilderFactory().getGsonBuilder().create();

		CaseDefinitionController controller = new CaseDefinitionController();
		ReflectionTestUtils.setField(controller, "caseDefinitionService", caseDefinitionService);

		// Gson for HTTP conversion, as in production (models carry Gson types like
		// JsonObject that Jackson can't serialize).
		this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setMessageConverters(new GsonHttpMessageConverter(gson))
				.build();
	}

	@Test
	void serviceRejectionSurfacesAs400() throws Exception {
		when(caseDefinitionService.create(any())).thenThrow(new ConfigValidationException(
				ConfigDocType.CASE_DEFINITION, "customer-support", List.of("formKey: is missing but it is required")));

		mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":\"customer-support\",\"name\":\"Customer Support\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Bad Request"));
	}

	@Test
	void successfulWriteReturns200() throws Exception {
		when(caseDefinitionService.create(any())).thenReturn(new CaseDefinition());
		mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":\"customer-support\",\"name\":\"Customer Support\",\"formKey\":\"cs-form\"}"))
				.andExpect(status().isOk());
	}
}
