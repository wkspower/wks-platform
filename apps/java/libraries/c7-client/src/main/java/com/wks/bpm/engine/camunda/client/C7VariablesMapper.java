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
package com.wks.bpm.engine.camunda.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.community.rest.client.dto.VariableValueDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@Component
@Qualifier("c7VariablesMapper")
public class C7VariablesMapper implements VariablesMapper<Map<String, VariableValueDto>> {

	@Override
	public Map<String, VariableValueDto> toEngineFormat(final List<ProcessVariable> processVariables) {
		Map<String, VariableValueDto> variableValueMap = new HashMap<>();

		for (ProcessVariable processVariable : processVariables) {
			VariableValueDto variableValueDto = new VariableValueDto();
			variableValueDto.setValue(processVariable.getValue());
			variableValueDto.setType(processVariable.getType());
			variableValueMap.put(processVariable.getName(), variableValueDto);
		}

		return variableValueMap;
	}

}
