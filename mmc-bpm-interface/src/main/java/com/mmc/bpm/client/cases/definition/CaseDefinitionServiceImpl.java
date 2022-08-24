package com.mmc.bpm.client.cases.definition;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.client.cases.instance.CaseInstanceNotFoundException;
import com.mmc.bpm.client.repository.DataRepository;

@Component
public class CaseDefinitionServiceImpl implements CaseDefinitionService {

	@Autowired
	private DataRepository dataRepository;

	@Override
	public List<CaseDefinition> find() throws Exception {
		return dataRepository.findCaseDefintions();
	}

	@Override
	public CaseDefinition get(final String caseDefId) throws Exception {
		return dataRepository.getCaseDefinition(caseDefId);
	}

	@Override
	public CaseDefinition create(final CaseDefinition caseDefinition) throws Exception {
		dataRepository.saveCaseDefinition(caseDefinition);
		return caseDefinition;
	}

	@Override
	public void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception {
		dataRepository.deleteCaseDefinition(caseDefinitionId);
	}

	public void setDataRepository(DataRepository dataRepository) {
		this.dataRepository = dataRepository;
	}

}
