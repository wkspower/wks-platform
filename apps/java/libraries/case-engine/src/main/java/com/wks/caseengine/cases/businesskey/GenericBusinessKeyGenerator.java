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
package com.wks.caseengine.cases.businesskey;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public static final String PREFIX = "";

	@Override
	public String generate() {
		return calculateBusinessKey();
	}

	private String calculateBusinessKey() {
		return String.valueOf(PREFIX + ThreadLocalRandom.current().nextInt(0, 100000 + 1));
	}

}
