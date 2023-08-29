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
package com.wks.caseengine.cases.instance.service;

import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.repository.Repository;

@Component
class CaseInstanceCreateServiceImpl implements CaseInstanceCreateService {

	@Autowired
	private Repository<CaseInstance> repository;

	@Autowired
	private Repository<CaseDefinition> caseDefRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Override
	public CaseInstance create(final CaseInstance caseInstanceParam) throws Exception {
		CaseDefinition caseDefinition = caseDefRepository.get(caseInstanceParam.getCaseDefinitionId());
		if (caseDefinition == null) {
			throw new CaseDefinitionNotFoundException();
		}

		String businessKey = null;
		if (caseInstanceParam.getBusinessKey() == null) {
			businessKey = businessKeyCreator.generate();
		} else {
			businessKey = caseInstanceParam.getBusinessKey();
		}

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.stage(caseDefinition.getStages().stream().sorted(Comparator.comparing(CaseStage::getIndex)).findFirst()
						.get().getName())
				.attributes(caseInstanceParam.getAttributes()).caseDefinitionId(caseDefinition.getId())
				.caseOwner(caseInstanceParam.getCaseOwner()).caseOwnerName(caseInstanceParam.getCaseOwnerName())
				.build();

		repository.save(caseInstance);

		return caseInstance;
	}

	@Override
	public CaseInstance create(final CaseDefinition caseDefinition) throws Exception {
		if (caseDefinition == null) {
			throw new CaseDefinitionNotFoundException();
		}

		String businessKey = businessKeyCreator.generate();
		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.stage(caseDefinition.getStages().stream().sorted(Comparator.comparing(CaseStage::getIndex)).findFirst()
						.get().getName())
				.attributes(new ArrayList<>()).caseDefinitionId(caseDefinition.getId()).build();

		repository.save(caseInstance);

		return caseInstance;
	}

	public void setRepository(Repository<CaseInstance> repository) {
		this.repository = repository;
	}

	public void setBusinessKeyCreator(GenericBusinessKeyGenerator businessKeyCreator) {
		this.businessKeyCreator = businessKeyCreator;
	}

}
