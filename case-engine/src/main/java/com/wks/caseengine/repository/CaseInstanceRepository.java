package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status, final Optional<String> caseDefinitionId) throws Exception;

}
