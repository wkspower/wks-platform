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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;

@Component
public class CaseEmailServiceImpl implements CaseEmailService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public List<CaseEmail> find(Optional<String> businessKey) {
		return commandExecutor.execute(new FindCaseEmailCmd(businessKey));
	}

	@Override
	public void start(CaseEmail caseEmail) {
		if (!caseEmail.isOutbound()) {
			commandExecutor.execute(new StartCaseEmailCmd(caseEmail));
		} else {
			commandExecutor.execute(new StartCaseEmailOutboundCmd(caseEmail));
		}
	}

	@Override
	public CaseEmail save(CaseEmail caseEmail) {
		return commandExecutor.execute(new SaveCaseEmailCmd(caseEmail));
	}

	@Override
	public void markAsSent(final String id) {
		commandExecutor.execute(new MarkAsSentCaseEmailCmd(id));
	}

	@Override
	public void patch(final String id, final CaseEmail mergePatch) {
		commandExecutor.execute(new PatchCaseEmailCmd(id, mergePatch));
	}
}
