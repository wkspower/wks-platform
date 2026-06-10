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
package com.wks.caseengine.cases.instance.persistence;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.ProcessVariableType;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.process.instance.ProcessInstanceService;

/**
 * Default persistence strategy ({@code wks.bpm.engine=camunda7} or absent): start
 * the case-creation process and let the workflow write the case via the
 * {@code caseSave} external task. The case is NOT written here — that mirrors the
 * single write point that exists when a workflow engine is present.
 */
@Component
@ConditionalOnProperty(name = "wks.bpm.engine", havingValue = "camunda7", matchIfMissing = true)
public class WorkflowCasePersistenceStrategy implements CasePersistenceStrategy {

	private final ProcessInstanceService processInstanceService;

	private final GsonBuilder gsonBuilder;

	private final String caseCreationProcess;

	public WorkflowCasePersistenceStrategy(ProcessInstanceService processInstanceService, GsonBuilder gsonBuilder,
			@Value("${case.engine.case-creation-process}") String caseCreationProcess) {
		this.processInstanceService = processInstanceService;
		this.gsonBuilder = gsonBuilder;
		this.caseCreationProcess = caseCreationProcess;
	}

	@Override
	public CaseInstance persist(CaseInstance preparedCaseInstance) {
		Gson gson = gsonBuilder.create();
		ProcessVariable caseInstanceProcessVariable = ProcessVariable.builder()
				.type(ProcessVariableType.JSON.getValue()).name("caseInstance")
				.value(gson.toJsonTree(preparedCaseInstance).toString()).build();

		processInstanceService.start(caseCreationProcess,
				Optional.ofNullable(preparedCaseInstance.getBusinessKey()),
				Optional.of(caseInstanceProcessVariable));

		return preparedCaseInstance;
	}

}
