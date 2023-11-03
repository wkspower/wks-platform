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
package com.wks.caseengine.record.command;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.record.RecordNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class GetRecordCmd implements Command<JsonObject> {

	private String recordTypeId;
	private String recordId;

	@Override
	public JsonObject execute(CommandContext commandContext) {
		try {
			return commandContext.getRecordRepository().get(recordTypeId, recordId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new RecordNotFoundException(e.getMessage(), e);
		}
	}

}
