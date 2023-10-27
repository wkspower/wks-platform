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
package com.wks.caseengine.cases.definition.command;

import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@Builder
public class FindCaseDefinitionFilter {
	
	private Optional<Boolean> deployed;

}
