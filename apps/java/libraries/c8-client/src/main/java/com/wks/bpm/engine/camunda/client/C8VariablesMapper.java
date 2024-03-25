/*
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@Component
@Qualifier("c8VariablesMapper")
public class C8VariablesMapper implements VariablesMapper<Map<String, String>> {

	@Override
	public Map<String, String> toEngineFormat(final List<ProcessVariable> caseAttributes) {

		Map<String, String> processVariablesMap = new LinkedHashMap<>();

		caseAttributes.forEach(caseAttribute -> {
			processVariablesMap.put(caseAttribute.getName(), caseAttribute.getValue());
		});

		return processVariablesMap;
	}

}
