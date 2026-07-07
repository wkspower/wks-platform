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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseDocument {

	private String name;

	private String type;

	private String size;

	private String base64;

	/**
	 * Optional id of the {@code requiredDocuments} entry on the case definition
	 * that this document satisfies (see
	 * {@link com.wks.caseengine.cases.definition.DocumentRequirement}). Null means
	 * a free-form attachment not tied to a declared requirement.
	 */
	private String requirementId;
}
