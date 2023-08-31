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

import java.io.Serializable;

public enum CaseAttributeType implements Serializable {

	STRING("String"), JSON("Json");

	private String value;

	private CaseAttributeType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
