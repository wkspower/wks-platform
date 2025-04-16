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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface CaseEmailService {

	AOPMessageVM find(final Optional<String> businessKey);

	AOPMessageVM start(final CaseEmail caseEmail);

	AOPMessageVM save(final CaseEmail caseEmail);

	AOPMessageVM markAsSent(final String id, final Date sentDateTime);

	AOPMessageVM patch(final String id, final CaseEmail mergePatch);


}
