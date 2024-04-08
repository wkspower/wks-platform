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
package com.wks.caseengine.cases.instance.email;

import java.util.List;
import java.util.Optional;

public interface CaseEmailService {

	List<CaseEmail> find(final Optional<String> businessKey);

	void start(final CaseEmail caseEmail);

	CaseEmail save(final CaseEmail caseEmail);

	void markAsSent(final String id);

	void patch(final String id, final CaseEmail mergePatch);


}
