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

import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;

public interface ProcessInstanceService {

	public ProcessInstance start(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable);

	public ProcessInstance start(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables);
	
	void delete(final List<ProcessInstance> processInstances);

	void delete(final String processInstanceId);

	List<ProcessInstance> find(final Optional<String> processDefinitionKey, final Optional<String> businessKey,
			final Optional<String> activityIdIn);

	List<ActivityInstance> getActivityInstances(final String processInstanceId);

}
