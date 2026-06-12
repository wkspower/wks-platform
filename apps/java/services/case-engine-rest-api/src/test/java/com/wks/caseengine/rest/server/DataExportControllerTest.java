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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.data.export.DataExportService;
import com.wks.caseengine.rest.mocks.MockSecurityContext;
import com.wks.caseengine.rest.server.data.DataExportController;

@WebMvcTest(controllers = DataExportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataExportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DataExportService dataExportService;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldExport() throws Exception {
		this.mockMvc.perform(get("/export")).andExpect(status().isOk());
	}

}
