package com.wks.caseengine.cases.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.repository.DataRepository;

@Component
class CaseInstanceCreateServiceImpl implements CaseInstanceCreateService {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Override
	public CaseInstance create(final CaseInstance caseInstanceParam) throws Exception {
		CaseDefinition caseDefinition = dataRepository.getCaseDefinition(caseInstanceParam.getCaseDefinitionId());
		if (caseDefinition == null) {
			throw new CaseDefinitionNotFoundException();
		}

		String businessKey = businessKeyCreator.generate();
		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.attributes(caseInstanceParam.getAttributes()).caseDefinitionId(caseDefinition.getId()).build();

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;
	}

	public void setDataRepository(DataRepository dataRepository) {
		this.dataRepository = dataRepository;
	}

	public void setBusinessKeyCreator(GenericBusinessKeyGenerator businessKeyCreator) {
		this.businessKeyCreator = businessKeyCreator;
	}

}
