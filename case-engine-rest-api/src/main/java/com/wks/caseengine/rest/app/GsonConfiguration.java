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
package com.wks.caseengine.rest.app;

import java.util.List;

import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.wks.caseengine.cases.definition.action.CaseAction;
import com.wks.caseengine.cases.definition.action.CaseActionDeserializer;
import com.wks.caseengine.cases.definition.action.CaseActionSerializer;

@Configuration
public class GsonConfiguration {

	@Bean
	public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(CaseAction.class, new CaseActionDeserializer());
		builder.registerTypeAdapter(CaseAction.class, new CaseActionSerializer<>());

		return builder;
	}

}
