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
package com.wks.bpm.engine.camunda.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.BpmEngineType;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.exception.BpmEngineDeploymentException;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@ExtendWith(MockitoExtension.class)
public class C7EngineClientTest {

	private static final String BPMN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Definitions_1\">"
			+ "<bpmn:process id=\"demoProc\" name=\"Demo Proc\" isExecutable=\"true\">"
			+ "<bpmn:startEvent id=\"StartEvent_1\"/></bpmn:process></bpmn:definitions>";

	@Mock
	private DeploymentApi deploymentApi;

	@Mock
	private ProcessDefinitionApi processDefinitionApi;

	@Mock
	private SecurityContextTenantHolder tenantHolder;

	@Mock
	private VariablesMapper<Map<String, VariableValueDto>> c7VariablesMapper;

	@InjectMocks
	private C7EngineClient client;

	private final BpmEngine bpmEngine = new BpmEngine() {

		@Override
		public String getId() {
			return "test-engine";
		}

		@Override
		public String getName() {
			return "Test Engine";
		}

		@Override
		public BpmEngineType getType() {
			return BpmEngineType.BPM_ENGINE_CAMUNDA7;
		}
	};

	@BeforeEach
	void setUp() {
		lenient().when(tenantHolder.getTenantId()).thenReturn(Optional.of("wks"));
	}

	/**
	 * Regression: every save of an existing process minted a fresh deployment and
	 * a new process definition version, so editing a process appeared to clone it.
	 * Duplicate filtering only works when the resource keeps a stable name, hence
	 * both assertions belong together.
	 */
	@Test
	void shouldDeployWithDuplicateFilteringUnderAStableResourceName() throws Exception {
		client.deploy(bpmEngine, "fileName.bpmn", BPMN);

		ArgumentCaptor<String> resourceName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<File> staged = ArgumentCaptor.forClass(File.class);
		verify(deploymentApi).createDeployment(eq("wks"), anyString(), eq(Boolean.TRUE), eq(Boolean.TRUE),
				resourceName.capture(), any(OffsetDateTime.class), staged.capture());

		assertEquals("demoProc.bpmn", resourceName.getValue());
		// Camunda derives the resource name from the uploaded file name, so a
		// randomly-named temp file silently defeats duplicate filtering.
		assertEquals("demoProc.bpmn", staged.getValue().getName());
	}

	/** A process id is used as a file name; it must not be able to escape the staging dir. */
	@Test
	void shouldSanitiseAProcessIdThatIsNotFileNameSafe() throws Exception {
		client.deploy(bpmEngine, "fileName.bpmn",
				BPMN.replace("id=\"demoProc\"", "id=\"../../etc/evil\""));

		ArgumentCaptor<File> staged = ArgumentCaptor.forClass(File.class);
		verify(deploymentApi).createDeployment(anyString(), anyString(), any(), any(), anyString(),
				any(OffsetDateTime.class), staged.capture());

		assertEquals(".._.._etc_evil.bpmn", staged.getValue().getName());
	}

	/** Unparseable XML still reaches the engine, which reports the real problem. */
	@Test
	void shouldFallBackToTheGivenResourceNameWhenTheProcessIdCannotBeRead() throws Exception {
		client.deploy(bpmEngine, "fileName.bpmn", "not xml at all");

		verify(deploymentApi).createDeployment(eq("wks"), anyString(), eq(Boolean.TRUE), eq(Boolean.TRUE),
				eq("fileName.bpmn"), any(OffsetDateTime.class), any(File.class));
	}

	/**
	 * Regression: the engine's rejection (a missing history TTL, say) was logged
	 * and swallowed, so the caller was told the deployment had succeeded.
	 */
	@Test
	void shouldRaiseWhenTheEngineRejectsTheDeployment() throws Exception {
		when(deploymentApi.createDeployment(anyString(), anyString(), any(), any(), anyString(),
				any(OffsetDateTime.class), any(File.class)))
						.thenThrow(new ApiException(400, "Bad Request", Collections.emptyMap(),
								"ENGINE-12018 History Time To Live (TTL) cannot be null"));

		BpmEngineDeploymentException thrown = assertThrows(BpmEngineDeploymentException.class,
				() -> client.deploy(bpmEngine, "fileName.bpmn", BPMN));

		assertTrue(thrown.getMessage().contains("ENGINE-12018"),
				"the engine's explanation must survive: " + thrown.getMessage());
	}

	/**
	 * Regression: starting a process with no variables — what the case form's
	 * manual start sends — threw NoSuchElementException on the empty Optional.
	 */
	@Test
	void shouldStartProcessWhenNoProcessVariableIsSupplied() throws Exception {
		when(c7VariablesMapper.toEngineFormat(List.<ProcessVariable>of())).thenReturn(Map.of());
		when(processDefinitionApi.startProcessInstanceByKeyAndTenantId(eq("demoProc"), eq("wks"),
				any(StartProcessInstanceDto.class)))
						.thenReturn(new ProcessInstanceWithVariablesDto().id("pi-1").businessKey("CASE-1"));

		ProcessInstance started = client.startProcess("demoProc", Optional.of("CASE-1"),
				Optional.<ProcessVariable>empty(), bpmEngine);

		assertEquals("pi-1", started.getId());
		assertEquals("CASE-1", started.getBusinessKey());
	}

}
