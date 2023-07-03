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
package com.wks.caseengine.cases.definition;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;

public interface CaseDefinitionService {

	List<CaseDefinition> find() throws Exception;

	List<CaseDefinition> find(final Optional<Boolean> deployed) throws Exception;

	CaseDefinition get(final String caseDefId) throws Exception;

	CaseDefinition create(final CaseDefinition caseDefinition) throws Exception;

	CaseDefinition update(final String caseDefId, CaseDefinition caseDefinition) throws Exception;

	void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception;

}
