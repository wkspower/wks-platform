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
package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.form.FormRepository;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.queue.QueueRepository;
import com.wks.caseengine.record.RecordRepository;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Component
@Getter
public class CommandContext {

	@Autowired
	private SecurityContextTenantHolder securityContextTenantHolder;

	@Autowired
	private CaseDefinitionRepository caseDefRepository;

	@Autowired
	private CaseInstanceRepository caseInstanceRepository;

	@Autowired
	private FormRepository formRepository;

	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private RecordRepository recordRepository;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

}
