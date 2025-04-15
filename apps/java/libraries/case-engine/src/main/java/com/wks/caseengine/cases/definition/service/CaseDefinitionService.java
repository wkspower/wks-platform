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
package com.wks.caseengine.cases.definition.service;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CaseDefinitionService {

	AOPMessageVM find(final Optional<Boolean> deployed);

	AOPMessageVM get(final String caseDefId);

	AOPMessageVM create(final CaseDefinition caseDefinition);

	AOPMessageVM update(final String caseDefId, CaseDefinition caseDefinition);

	void delete(final String caseDefinitionId);

}
