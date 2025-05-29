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
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.record.command.CreasteRecordCmd;
import com.wks.caseengine.record.command.DeleteRecordCmd;
import com.wks.caseengine.record.command.FindRecordCmd;
import com.wks.caseengine.record.command.GetRecordCmd;
import com.wks.caseengine.record.command.UpdateRecordCmd;

@Component
public class RecordServiceImpl implements RecordService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	@Transactional
	public void save(final String recordTypeId, final JsonObject record){
		commandExecutor.execute(new CreasteRecordCmd(recordTypeId, record));
	}

	@Override
	public JsonObject get(final String recordTypeId, final String id){
		return commandExecutor.execute(new GetRecordCmd(recordTypeId, recordTypeId));
	}

	@Override
	public List<JsonObject> find(final String recordTypeId){
		return commandExecutor.execute(new FindRecordCmd(recordTypeId));
	}

	@Override
	@Transactional
	public void delete(final String recordTypeId, final String id){
		commandExecutor.execute(new DeleteRecordCmd(recordTypeId, recordTypeId));

	}

	@Override
	@Transactional
	public void update(final String recordTypeId, final String id, final JsonObject record){
		commandExecutor.execute(new UpdateRecordCmd(recordTypeId, id, record));
	}

}
