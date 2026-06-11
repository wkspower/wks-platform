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
package com.wks.caseengine.cases.instance.command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseAttributeType;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;
import com.wks.caseengine.command.AuditableCommand;
import com.wks.caseengine.audit.AuditEventType;
import java.util.Map;
import java.util.HashMap;

/**
 * @author victor.franca
 *
 */
public class StartCaseInstanceWithValuesCmd implements AuditableCommand<CaseInstance> {

	private CaseInstance caseInstanceParam;
	private String businessKeyGenerated;

	public StartCaseInstanceWithValuesCmd(CaseInstance caseInstanceParam) {
		this.caseInstanceParam = caseInstanceParam;
	}

	public void setCaseInstanceParam(CaseInstance caseInstanceParam) {
		this.caseInstanceParam = caseInstanceParam;
	}

	@Override
	public CaseInstance execute(CommandContext commandContext) {

		CaseDefinition caseDefinition = retrieveCaseDefinition(commandContext);

		caseInstanceParam.addAttribute(
				new CaseAttribute("createdAt", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						CaseAttributeType.STRING.getValue()));

		String businessKey = generateBusinessKey(commandContext);
		this.businessKeyGenerated = businessKey;

		CaseInstance.CaseInstanceBuilder caseInstanceBuilder = CaseInstance.builder().businessKey(businessKey)
				.attributes(caseInstanceParam.getAttributes()).caseDefinitionId(caseInstanceParam.getCaseDefinitionId())
				.owner(caseInstanceParam.getOwner());

		Optional<CaseStage> firstStage = caseDefinition.getStages().stream()
				.sorted(Comparator.comparing(CaseStage::getIndex)).findFirst();
		if (firstStage.isPresent()) {
			caseInstanceBuilder.stage(firstStage.get().getName());
		}

		CaseInstance preparedCaseInstance = caseInstanceBuilder.build();

		// Persistence is delegated to the active CasePersistenceStrategy
		// (workflow round-trip vs direct save), selected by wks.bpm.engine.
		return commandContext.getCasePersistenceStrategy().persist(preparedCaseInstance);
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.CASE_CREATED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKeyGenerated != null ? businessKeyGenerated : caseInstanceParam.getBusinessKey();
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, CaseInstance result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (result != null) {
			payloadMap.put("businessKey", result.getBusinessKey());
			payloadMap.put("caseDefinitionId", result.getCaseDefinitionId());
			payloadMap.put("stage", result.getStage());
			payloadMap.put("status", result.getStatus() != null ? result.getStatus().getCode() : null);
			payloadMap.put("owner", result.getOwner() != null ? result.getOwner().getName() : null);
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

	private String generateBusinessKey(CommandContext commandContext) {
		String businessKey = null;
		if (caseInstanceParam.getBusinessKey() == null) {
			businessKey = commandContext.getBusinessKeyCreator().generate();
		} else {
			businessKey = caseInstanceParam.getBusinessKey();
		}
		return businessKey;
	}

	private CaseDefinition retrieveCaseDefinition(CommandContext commandContext) {
		CaseDefinition caseDefinition;
		try {
			caseDefinition = commandContext.getCaseDefRepository().get(caseInstanceParam.getCaseDefinitionId());
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e.getMessage(), e);
		}
		return caseDefinition;
	}

}
