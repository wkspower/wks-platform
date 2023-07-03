package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;

public interface CaseDefinitionRepository extends Repository<CaseDefinition> {

	List<CaseDefinition> find(final Optional<Boolean> deployed) throws Exception;

}
