package com.wks.caseengine.cases.definition;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.repository.CaseDefinitionRepository;

@Component
public class CaseDefinitionServiceImpl implements CaseDefinitionService {

	@Autowired
	private CaseDefinitionRepository repository;

	@Override
	public List<CaseDefinition> find() throws Exception {
		return repository.find();
	}

	@Override
	public CaseDefinition get(final String caseDefId) throws Exception {
		return repository.get(caseDefId);
	}

	@Override
	public CaseDefinition create(final CaseDefinition caseDefinition) throws Exception {
		if (caseDefinition.getId().isEmpty()) {
			// TODO error handling
			throw new Exception("No Case Definition ID provided");
		}
		repository.save(caseDefinition);
		return caseDefinition;
	}

	@Override
	public CaseDefinition update(final String caseDefId, final CaseDefinition caseDefinition) throws Exception {
		repository.update(caseDefId, caseDefinition);
		return caseDefinition;
	}

	@Override
	public void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception {
		repository.delete(caseDefinitionId);
	}

	public void setRepository(CaseDefinitionRepository repository) {
		this.repository = repository;
	}

}
