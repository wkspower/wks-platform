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
package com.wks.bpm.engine.model.spi;

import lombok.Getter;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
public class Task {

	protected String id;
	protected String owner;
	protected String assignee;
	protected String name;
	protected String description;
	protected String priority;
	protected String tenantId;
	protected String executionId;
	protected String processInstanceId;
	protected String processDefinitionId;
	protected String caseExecutionId;
	protected String caseInstanceId;
	protected String caseDefinitionId;
	protected String taskDefinitionKey;
	protected String formKey;

	protected String created;
	protected String due;
	protected String followUp;

}
