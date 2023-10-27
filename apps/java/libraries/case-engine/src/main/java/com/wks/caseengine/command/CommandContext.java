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
package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Component
@Getter
public class CommandContext {

	@Autowired
	private SecurityContextTenantHolder securityContextTenantHolder;

	@Autowired
	private CaseDefinitionRepository caseDefRepository;

}
