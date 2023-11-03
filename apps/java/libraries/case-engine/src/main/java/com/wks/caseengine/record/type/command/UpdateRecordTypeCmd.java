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
package com.wks.caseengine.record.type.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.record.type.RecordType;
import com.wks.caseengine.record.type.RecordTypeNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class UpdateRecordTypeCmd implements Command<Void> {

	private String recordTypeId;
	private RecordType recordType;

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			commandContext.getRecordTypeRepository().update(recordTypeId, recordType);
		} catch (DatabaseRecordNotFoundException e) {
			throw new RecordTypeNotFoundException(e.getMessage(), e);
		}
		return null;
	}

}
