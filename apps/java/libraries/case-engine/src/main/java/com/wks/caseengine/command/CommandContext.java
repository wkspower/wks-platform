package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;

import lombok.Getter;

@Component
@Getter
public class CommandContext {

	@Autowired
	private SecurityContextTenantHolder securityContextTenantHolder;

	@Autowired
	private CaseDefinitionRepository caseDefRepository;

}
