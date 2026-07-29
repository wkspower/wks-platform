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
package com.wks.caseengine.process.instance;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStageProcessDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * Starts the processes a case definition marks {@code autoStart} on the stage a
 * case has just entered.
 *
 * <p>The stage's {@code processesDefinitions} carry an {@code autoStart} flag,
 * but nothing acted on it: {@code autoStart=false} entries were offered in the
 * case form's manual "Start process" dialog and {@code autoStart=true} entries
 * were simply never started. This is the missing half — it runs when a case
 * enters its first stage and on every subsequent stage transition.
 *
 * @author victor.franca
 */
@Slf4j
@Component
public class CaseStageProcessStarter {

	@Autowired
	private ProcessInstanceService processInstanceService;

	/**
	 * Starts every {@code autoStart} process configured on {@code stageName},
	 * correlated to the case by its business key.
	 *
	 * <p>A process that fails to start is logged and skipped rather than aborting
	 * the case transition that triggered it: losing the case (or leaving it
	 * wedged between stages) is worse than losing one of its processes.
	 */
	public void startAutoStartProcesses(final CaseDefinition caseDefinition, final String stageName,
			final String businessKey) {

		if (caseDefinition == null || stageName == null || businessKey == null) {
			return;
		}

		for (CaseStageProcessDefinition process : autoStartProcessesOf(caseDefinition, stageName)) {
			String definitionKey = process.getDefinitionKey();
			if (definitionKey == null || definitionKey.isBlank()) {
				continue;
			}

			try {
				processInstanceService.start(definitionKey, Optional.of(businessKey), Collections.emptyList());
				log.debug("Auto-started process {} for case {} entering stage {}", definitionKey, businessKey,
						stageName);
			} catch (Exception e) {
				log.error("Could not auto-start process {} for case {} entering stage {}", definitionKey, businessKey,
						stageName, e);
			}
		}
	}

	private List<CaseStageProcessDefinition> autoStartProcessesOf(final CaseDefinition caseDefinition,
			final String stageName) {

		if (caseDefinition.getStages() == null) {
			return Collections.emptyList();
		}

		return caseDefinition.getStages().stream().filter(stage -> stageName.equals(stage.getName()))
				.map(CaseStage::getProcessesDefinitions).filter(processes -> processes != null)
				.flatMap(List::stream).filter(CaseStageProcessDefinition::isAutoStart).toList();
	}

}
