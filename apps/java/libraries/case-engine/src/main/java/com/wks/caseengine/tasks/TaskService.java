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
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Product;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TaskService {

	AOPMessageVM create(final Task task);

	AOPMessageVM find(final Optional<String> processInstanceBusinessKey);

	AOPMessageVM claim(final String taskId, final String taskAssignee);

	AOPMessageVM unclaim(final String taskId);

	AOPMessageVM complete(final String taskId, final List<ProcessVariable> variables);

}
