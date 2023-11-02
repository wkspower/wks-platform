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
package com.wks.caseengine.record.type;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.record.type.command.CreateRecordTypeCmd;
import com.wks.caseengine.record.type.command.DeleteRecordTypeCmd;
import com.wks.caseengine.record.type.command.FindRecordTypeCmd;
import com.wks.caseengine.record.type.command.GetRecordTypeCmd;
import com.wks.caseengine.record.type.command.UpdateRecordTypeCmd;

@Component
public class RecordTypeServiceImpl implements RecordTypeService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void save(RecordType recordType){
		commandExecutor.execute(new CreateRecordTypeCmd(recordType));
	}

	@Override
	public RecordType get(String id){
		return commandExecutor.execute(new GetRecordTypeCmd(id));
	}

	@Override
	public List<RecordType> find(){
		return commandExecutor.execute(new FindRecordTypeCmd());
	}

	@Override
	public void delete(String id){
		commandExecutor.execute(new DeleteRecordTypeCmd(id));
	}

	@Override
	public void update(final String id, final RecordType recordType) {
		commandExecutor.execute(new UpdateRecordTypeCmd(id, recordType));
	}

}
