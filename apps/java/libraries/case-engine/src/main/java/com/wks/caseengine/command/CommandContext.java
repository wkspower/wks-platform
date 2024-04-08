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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.email.repository.CaseEmailRepository;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.form.FormRepository;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.queue.QueueRepository;
import com.wks.caseengine.record.RecordRepository;
import com.wks.caseengine.record.type.RecordTypeRepository;

import lombok.Getter;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Component
@Getter
@Setter
public class CommandContext {

	@Value("${case.engine.case-creation-process}")
	private String caseCreationProcess;

	@Value("${case.engine.email-to-case-process}")
	private String emailToCaseProcess;

	@Value("${case.engine.email-to-case-outbound-process}")
	private String emailToCaseOutboundProcess;

	@Autowired
	private SecurityContextTenantHolder securityContextTenantHolder;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private CaseDefinitionRepository caseDefRepository;

	@Autowired
	private CaseInstanceRepository caseInstanceRepository;

	@Autowired
	private CaseEmailRepository caseEmailRepository;

	@Autowired
	private FormRepository formRepository;

	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private RecordRepository recordRepository;

	@Autowired
	private RecordTypeRepository recordTypeRepository;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private BpmEngineClientFacade bpmEngineClientFacade;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

}
