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

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.message.vm.AOPMessageVM;

@Component
public class CaseEmailServiceImpl implements CaseEmailService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public AOPMessageVM find(Optional<String> businessKey) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			List<CaseEmail> emails = commandExecutor.execute(new FindCaseEmailCmd(businessKey));
			response.setCode(200);
			response.setMessage("Emails fetched successfully");
			response.setData(emails);

		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Failed to fetch emails: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM start(CaseEmail caseEmail) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			if (!caseEmail.isOutbound()) {
				commandExecutor.execute(new StartCaseEmailCmd(caseEmail));
			} else {
				commandExecutor.execute(new StartCaseEmailOutboundCmd(caseEmail));
			}
			response.setCode(200); // No Content
			response.setMessage("Email process started successfully.");
			response.setData(null);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Failed to start email process: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM save(CaseEmail caseEmail) {
		CaseEmail savedEmail = commandExecutor.execute(new SaveCaseEmailCmd(caseEmail));
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Case Email Saved Successfully... ");
		response.setData(savedEmail);

		return response;
	}

	@Override
	public AOPMessageVM markAsSent(final String id, final Date sentDateTime) {
		commandExecutor.execute(new MarkAsSentCaseEmailCmd(id, sentDateTime));
		AOPMessageVM response = new AOPMessageVM();

		response.setCode(200);
		response.setMessage("Marked email is sent successfully...");

		return response;
	}

	@Override
	public AOPMessageVM patch(final String id, final CaseEmail mergePatch) {
		commandExecutor.execute(new PatchCaseEmailCmd(id, mergePatch));
		AOPMessageVM response = new AOPMessageVM();

		response.setCode(200);
		response.setMessage("Marked email is sent successfully...");

		return response;
	}
}
