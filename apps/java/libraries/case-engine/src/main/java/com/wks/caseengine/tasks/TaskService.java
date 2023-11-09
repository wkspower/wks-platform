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
package com.wks.caseengine.tasks;

import java.util.List;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;

public interface TaskService {

	void create(final Task task);

	List<Task> find(final String processInstanceBusinessKey);

	void claim(final String taskId, final String taskAssignee);

	void unclaim(final String taskId);

	void complete(final String taskId, final JsonObject variables);

}
