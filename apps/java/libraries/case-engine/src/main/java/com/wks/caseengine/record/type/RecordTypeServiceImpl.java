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
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.config.validation.ConfigDocType;
import com.wks.caseengine.config.validation.ConfigValidationService;
import com.wks.caseengine.record.type.command.CreateRecordTypeCmd;
import com.wks.caseengine.record.type.command.DeleteRecordTypeCmd;
import com.wks.caseengine.record.type.command.FindRecordTypeCmd;
import com.wks.caseengine.record.type.command.GetRecordTypeCmd;
import com.wks.caseengine.record.type.command.UpdateRecordTypeCmd;

@Component
public class RecordTypeServiceImpl implements RecordTypeService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private ConfigValidationService configValidationService;

	@Override
	@Transactional
	public void save(RecordType recordType){
		configValidationService.validateOnWrite(ConfigDocType.RECORD_TYPE, recordType, recordType.getId());
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
	@Transactional
	public void delete(String id){
		commandExecutor.execute(new DeleteRecordTypeCmd(id));
	}

	@Override
	@Transactional
	public void update(final String id, final RecordType recordType) {
		configValidationService.validateOnWrite(ConfigDocType.RECORD_TYPE, recordType, id);
		commandExecutor.execute(new UpdateRecordTypeCmd(id, recordType));
	}

}
