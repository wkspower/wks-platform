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
package com.wks.caseengine.variables;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface VariableService {

	AOPMessageVM findVariables(final String processInstanceId);

}
