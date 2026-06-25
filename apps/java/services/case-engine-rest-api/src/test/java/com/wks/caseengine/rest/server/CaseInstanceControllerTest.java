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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.gson.autoconfigure.GsonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@WebMvcTest(controllers = CaseInstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(GsonAutoConfiguration.class)
public class CaseInstanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CaseDefinitionRepository caseDefinitionRepository;

	@MockitoBean
	private CaseInstanceRepository caseInstanceRepository;

	@MockitoBean
	private BpmEngineClientFacade bpmEngineClientFacade;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldSaveCaseInstance() throws Exception {
		when(caseDefinitionRepository.get("CD-1")).thenReturn(new CaseDefinition());
		// Verify the deserialized payload actually flows through to the created entity
		// (the controller returns the persisted case), not just that the call returns 200.
		this.mockMvc.perform(post("/case").contentType(MediaType.APPLICATION_JSON).content("{\"caseDefinitionId\":\"CD-1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.caseDefinitionId").value("CD-1"))
				.andExpect(jsonPath("$.businessKey").exists());
	}

	@Test
	public void shouldRespond404_whenSavingCaseInstanceWithNoCaseDefinitionId() throws Exception {
		when(caseDefinitionRepository.get("CD-1")).thenThrow(CaseDefinitionNotFoundException.class);
		this.mockMvc.perform(post("/case").contentType(MediaType.APPLICATION_JSON).content("{\"caseDefinitionId\":\"CD-1\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldDeleteCaseInstance() throws Exception {
		this.mockMvc.perform(delete("/case/{businessKey}", "CI-1")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldMergePatchCaseInstance() throws Exception {
		this.mockMvc
				.perform(patch("/case/{businessKey}", "1").contentType("application/merge-patch+json").content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldApplyMergePatchFieldsAndPreserveStatus() throws Exception {
		// A patch touching only stage must update stage and leave the persisted status intact.
		// Capture what reaches the repository, not just the HTTP status.
		CaseInstance persisted = new CaseInstance(null, "1", "CD-1", "Data Collection", "WIP_CASE_STATUS");
		when(caseInstanceRepository.get("1")).thenReturn(persisted);

		this.mockMvc
				.perform(patch("/case/{businessKey}", "1").contentType("application/merge-patch+json")
						.content("{\"stage\":\"Review\"}"))
				.andExpect(status().isNoContent());

		ArgumentCaptor<CaseInstance> captor = ArgumentCaptor.forClass(CaseInstance.class);
		verify(caseInstanceRepository).update(eq("1"), captor.capture());

		CaseInstance saved = captor.getValue();
		assertEquals("Review", saved.getStage(), "patched stage must be applied");
		assertEquals(CaseStatus.WIP_CASE_STATUS, saved.getStatus(), "omitted status must be preserved");
	}

	@Test
	public void shouldGetCaseInstance() throws Exception {
		this.mockMvc.perform(get("/case/{businessKey}", "1")).andExpect(status().isOk());
	}

	@Test
	public void shouldFindCaseInstance() throws Exception {
		when(caseInstanceRepository.find(Mockito.any())).thenReturn(PageResult.<CaseInstance>builder().build());
		this.mockMvc.perform(get("/case")).andExpect(status().isOk());
	}

	@Test
	public void shouldSaveDocument() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(new CaseInstance());
		this.mockMvc
				.perform(
						post("/case/{businessKey}/document", "BK-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldSaveComment() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(new CaseInstance());
		this.mockMvc
				.perform(post("/case/{businessKey}/comment", "BK-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldUpdateComment() throws Exception {
		this.mockMvc.perform(put("/case/{businessKey}/comment/{commentId}", "1", "1")
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldDeleteComment() throws Exception {
		CaseInstance caseInstance = CaseInstance.builder().build();
		caseInstance.addComment(CaseComment.builder().id("Comment-1").build());

		when(caseInstanceRepository.get("BK-1")).thenReturn(caseInstance);
		this.mockMvc.perform(delete("/case/{businessKey}/comment/{commentId}", "BK-1", "Comment-1"))
				.andExpect(status().isNoContent());
	}

}
