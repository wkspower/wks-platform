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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@WebMvcTest(controllers = CaseDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CaseDefinitionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CaseDefinitionRepository caseDefinitionRepository;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldSaveNewCaseDefinition() throws Exception {
		this.mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON).content("{id: CD-1}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldRespond400_whenSavingWithNoCaseDefinnitionId() throws Exception {
		this.mockMvc.perform(post("/case-definition").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldDeleteCaseDefinition() throws Exception {
		this.mockMvc.perform(delete("/case-definition/{caseDefId}", "1")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldRespond404_whenDeletingWithInvalidCaseDefinnitionId() throws Exception {
		Mockito.doThrow(new DatabaseRecordNotFoundException(null, null, null)).when(caseDefinitionRepository)
				.delete("CD-1");
		this.mockMvc.perform(delete("/case-definition/{caseDefId}", "CD-1")).andExpect(status().isNotFound());
	}

	@Test
	public void shouldUpdateCaseDefinition() throws Exception {
		this.mockMvc
				.perform(put("/case-definition/{caseDefId}", "1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldRespond404_whenUpdatingWithInvalidCaseDefinnitionId() throws Exception {
		Mockito.doThrow(new DatabaseRecordNotFoundException(null, null, null)).when(caseDefinitionRepository)
				.update(eq("CD-1"), any());
		this.mockMvc.perform(
				put("/case-definition/{caseDefId}", "CD-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void shouldGetCaseDefinition() throws Exception {
		when(caseDefinitionRepository.get("CD-1")).thenReturn(new CaseDefinition());
		this.mockMvc.perform(get("/case-definition/{caseDefId}", "CD-1")).andExpect(status().isOk());
	}

	@Test
	public void shouldRespond404_whenGetWithInvalidCaseDefinnitionId() throws Exception {
		when(caseDefinitionRepository.get("CD-1")).thenThrow(DatabaseRecordNotFoundException.class );
		this.mockMvc.perform(get("/case-definition/{caseDefId}", "CD-1")).andExpect(status().isNotFound());
	}

	@Test
	public void shouldFindCaseDefinition() throws Exception {
		this.mockMvc.perform(get("/case-definition")).andExpect(status().isOk());
	}

}
