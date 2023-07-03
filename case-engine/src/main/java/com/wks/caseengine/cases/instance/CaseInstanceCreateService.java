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
package com.wks.caseengine.cases.instance;

import com.wks.caseengine.cases.definition.CaseDefinition;

interface CaseInstanceCreateService {

	CaseInstance create(final CaseInstance caseInstance) throws Exception;

	CaseInstance create(CaseDefinition caseDefinition) throws Exception;

}
