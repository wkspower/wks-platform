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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@Component
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Override
	public ProcessInstance start(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable) {
		return processEngineClient.startProcess(processDefinitionKey, businessKey, processVariable);
	}

	@Override
	public ProcessInstance start(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables) {
		return processEngineClient.startProcess(processDefinitionKey, businessKey, processVariables);
	}

	@Override
	public void delete(final String processInstanceId) {
		processEngineClient.deleteProcessInstance(processInstanceId);
	}

	@Override
	public void delete(final List<ProcessInstance> processInstances) {
		processInstances.forEach(o -> {
			delete(o.getId());
		});
	}

	@Override
	public List<ProcessInstance> find(final Optional<String> processDefinitionKey, final Optional<String> businessKey,
			final Optional<String> activityIdIn) {
		return Arrays.asList(processEngineClient.findProcessInstances(processDefinitionKey, businessKey, activityIdIn));
	}

	@Override
	public List<ActivityInstance> getActivityInstances(String processInstanceId) {
		return Arrays.asList(processEngineClient.findActivityInstances(processInstanceId));
	}

}
