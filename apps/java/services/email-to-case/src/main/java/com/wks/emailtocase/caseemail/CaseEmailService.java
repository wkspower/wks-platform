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
package com.wks.emailtocase.caseemail;

import java.util.List;
import java.util.Optional;

public interface CaseEmailService {

	void save(final CaseEmail caseEmail);

	List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey, final Optional<String> caseDefinitionId);

	CaseEmail get(final String caseEmailId);

}
