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
package com.wks.caseengine.record;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.record.command.DeleteRecordCmd;
import com.wks.caseengine.record.command.FindRecordCmd;
import com.wks.caseengine.record.command.GetRecordCmd;
import com.wks.caseengine.record.command.SaveRecordCmd;
import com.wks.caseengine.record.command.UpdateRecordCmd;

@Component
public class RecordServiceImpl implements RecordService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void save(final String recordTypeId, final JsonObject record) throws Exception {
		commandExecutor.execute(new SaveRecordCmd(recordTypeId, record));
	}

	@Override
	public JsonObject get(final String recordTypeId, final String id) throws Exception {
		return commandExecutor.execute(new GetRecordCmd(recordTypeId, recordTypeId));
	}

	@Override
	public List<JsonObject> find(final String recordTypeId) throws Exception {
		return commandExecutor.execute(new FindRecordCmd(recordTypeId));
	}

	@Override
	public void delete(final String recordTypeId, final String id) throws Exception {
		commandExecutor.execute(new DeleteRecordCmd(recordTypeId, recordTypeId));

	}

	@Override
	public void update(final String recordTypeId, final String id, final JsonObject record) throws Exception {
		commandExecutor.execute(new UpdateRecordCmd(recordTypeId, recordTypeId, record));

	}

}
